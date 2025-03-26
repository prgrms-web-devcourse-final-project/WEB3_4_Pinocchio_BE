import React, { useState, useEffect } from 'react';
import { FilePond } from 'react-filepond';
import styled from 'styled-components';
import 'filepond/dist/filepond.min.css';
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import { dateFormat } from "../../utils/utils";
import useConfirm from "../../hooks/useConfirm";
import { useToast } from "../../hooks/useToast";
import Spinner from '../Spinner';

const StyledFilePond = styled(FilePond)`
  .filepond--panel-root {
    background-color: #ffffff;
    color: #000000;
    border: 1px solid #ddd;
  }
  .filepond--drop-label {
    color: #000000;
  }
  .filepond--label-action {
    color: #6a9c89;
  }
  .filepond--item-panel {
    background-color: #f6f7f9;
    color: #000000;
  }
  .filepond--file-info {
    color: #6a9c89; /* 파일명 색상 변경 */
    text-decoration: underline; /* 링크처럼 보이게 밑줄 추가 */
    cursor: pointer !important; /* 강제 적용 */
    pointer-events: auto !important; /* 포인터 이벤트 허용 */
  }
  .filepond--file {
    height: 60px;
  }
`;

/*
  껍데기가 있는 경우에 사용하는 파일 업로드 컴포넌트 입니다.
  껍데기가 있다 = 게시물을 만들 때에 파일 업로드 없이 먼저 게시물 생성 -> 해당 게시물의 파일 추가 의 개념입니다.
  ex) 요청, 게시판 수정
*/

function YesMappingFile({
  label,            // 파일 업로드 안내 문구
  name = "files",   // 백엔드 컨트롤러에서 받는 파일의 이름
  maxFiles = 3,     // 최대 업로드 가능한 파일 개수
  moduleType,
  moduleItemId,
  jiraTicket = null,
  isDeleteButtonShow = true,
  allowFileDialog = true,
  allowDrop = true,
}) {
  const { openConfirm } = useConfirm();
  const showToast = useToast();
  // 권한
  const [isSystemAdmin, setIsSystemAdmin] = useState(false);
  const [isBoardAdmin, setIsBoardAdmin] = useState(false);
  const [userId, setUserId] = useState(0);

  // 업로드 대기 중인 파일 저장
  const pendingFiles = [];
  let uploadTimeout = null;

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      const decodedToken = jwtDecode(token);
      const roles = decodedToken?.roles || [];
      if (roles.includes("SYSTEM_ADMIN")) {
        setIsSystemAdmin(true);
      }
      if (roles.includes("BOARD_ADMIN")) {
        setIsBoardAdmin(true);
      }
      setUserId(decodedToken.userId);
    }
  }, []);

  useEffect(() => {
    if (!allowDrop) {
      const handleDragOver = (e) => e.preventDefault();
      const handleDrop = (e) => e.preventDefault();

      window.addEventListener('dragover', handleDragOver);
      window.addEventListener('drop', handleDrop);

      return () => {
        window.removeEventListener('dragover', handleDragOver);
        window.removeEventListener('drop', handleDrop);
      };
    }
  }, [allowDrop]);

  // 파일 목록 및 데이터를 통합 관리하는 상태 변수
  const [files, setFiles] = useState([]);
  const [isDownloading, setIsDownloading] = useState(false); // 다운로드 상태 관리

  const [validationInfo, setValidationInfo] = useState({
    maxFileSize: 0,          // 최대 파일 크기 (byte)
    allowedExtensions: [],    // 허용된 파일 확장자 목록
    invalidFileNameChars: [],   // 유효하지 않은 파일명 문자 목록
  });

  const getFileValidationInfo = async () => {
    try {
      const response = await axios.get(`/api/upload/validation-info`);
      const validationData = response.data;

      const fileSizeInfo = validationData.find(item => item.codeName === 'FILE_SIZE');
      const fileExtensionInfo = validationData.find(item => item.codeName === 'FILE_EXTENTION');
      const fileNameValidationInfo = validationData.find(item => item.codeName === 'FILE_NAME');

      setValidationInfo({
        maxFileSize: parseInt(fileSizeInfo.codeValue) * 1024 * 1024,
        allowedExtensions: fileExtensionInfo.codeValue.split(','),
        invalidFileNameChars: fileNameValidationInfo ? fileNameValidationInfo.codeValue.split(',') : [],
      });
    } catch (error) {
      console.error("Error fetching validation info", error);
    }
  };

  useEffect(() => {
    getFileValidationInfo();
    //      setFiles(prevFiles => prevFiles.filter(f => f.file !== file));
  }, []);

  // 파일 추가 전 검증 함수
  const validateFile = (fileItem) => {
    if (!fileItem || !fileItem.file) {
      openConfirm({
        title: '파일 정보가 유효하지 않습니다.',
        html: "유효한 파일을 선택해주세요."
      });

      return false;
    }

    const file = fileItem.file; // fileItem에서 원본 파일 객체를 가져옴
    if (!file || !file.name) {
      openConfirm({
        title: '파일 이름을 찾을 수 없습니다.',
        html: "유효한 파일을 업로드해주세요."
      });
      return false;
    }

    if (validationInfo.maxFileSize === 0 || validationInfo.allowedExtensions.length === 0) {
      // validationInfo가 아직 설정되지 않았을 때 경고
      console.warn("파일 검증이 유효한 상태가 아닙니다.");
      return false;
    }

    const fileExtension = file.name.split('.').pop().toLowerCase();

    // 파일 이름 유효성 검사
    for (const char of validationInfo.invalidFileNameChars) {
      if (file.name.includes(char)) {

        openConfirm({
          title: '잘못된 파일 이름입니다.',
          html: `파일 이름에 허용되지 않는 문자(${char})가 포함되어 있습니다.`
        });
        return false;
      }
    }

    // 파일 크기 검증
    if (file.size > validationInfo.maxFileSize) {

      openConfirm({
        title: '파일 크기 초과',
        html: `최대 ${validationInfo.maxFileSize / (1024 * 1024)}MB 크기 파일만 업로드할 수 있습니다.`
      });
      return false;
    }

    // 파일 확장자 검증
    if (!validationInfo.allowedExtensions.includes(fileExtension)) {

      openConfirm({
        title: '잘못된 파일 형식입니다.',
        html: `${validationInfo.allowedExtensions.join(', ')} 파일만 업로드할 수 있습니다.`
      });
      return false;
    }

    return true;
  };

  // 기존 파일 목록을 가져오는 함수
  const [isLoading, setIsLoading] = useState(false);
  const fetchExistingFiles = async () => {
    setIsLoading(true);
    if (!moduleItemId) return
    try {
      const response = await axios.get(`/api/upload/${moduleType}/${moduleItemId}/file/metadata`);
      const existingFiles = response.data;

      if (existingFiles && existingFiles.length > 0) {
        const filePondFiles = existingFiles.map(file => {
          const isUserFile = file.createUserId === userId; // 파일의 createUserId와 현재 사용자의 userId 비교
          return {
            source: file.originFileName,
            options: {
              type: 'local',
              file: {
                name: file.originFileName,
                size: file.fileSize,
                type: 'auto',
              }
            },
            metadata: {
              uploadId: file.uploadId,
              createUserId: file.createUserId,
              isUserFile: isUserFile, // 파일이 사용자가 업로드한 것인지 여부 추가
              uploadDate: dateFormat(file.createTime, "yyyy-MM-dd HH:mm a/p")
            }
          };
        });
        setFiles(filePondFiles);
      }
    } catch (err) {
      console.log(err);

      openConfirm({
        title: '파일 정보를 불러오는 중 오류가 발생했습니다.',
        html: `잠시 후 다시 시도해 주세요.`
      });
    } finally {
      setIsLoading(false);
    }
  };

  // 파일의 업로드 날짜 출력
  useEffect(() => {
    // 파일명 출력 클래스를 찾아 파일명을 가져와 files의 파일명과 비교하여 일치하는 데이터의 날짜를 추가함
    const fileInfoElements = document.querySelectorAll('.filepond--file-info');
    fileInfoElements.forEach((infoElement, index) => {
      // 파일명 출력 클래스
      const fileNameElement = infoElement.querySelector('.filepond--file-info-main');
      // 파일명
      const fileName = fileNameElement?.innerText?.trim();

      const file = files.find((f) => f.options?.file?.name === fileName);

      // 파일명 부모 태그(.filepond--file-info)에 날짜 출력하는 span 태그 추가
      if (file && file.metadata?.uploadDate) {
        let dateElement = infoElement.querySelector('.upload-date');
        if (!dateElement) {
          dateElement = document.createElement('span');
          dateElement.className = 'upload-date';
          infoElement.appendChild(dateElement);
        }
        dateElement.innerText = `${file.metadata.uploadDate}`;

        dateElement.style.fontSize = '12px'; // 글씨 크기
      }
    });
  }, [files]);

  // 기존 파일 목록을 가져오는 useEffect
  useEffect(() => {
    fetchExistingFiles();
  }, [moduleType, moduleItemId, userId]);

  // 파일 업로드 일괄 처리 함수
  const handleMultipleFileUpload = async (files, loads, errors) => {
    try {
      const uploadPromises = files.map((file, index) =>
        new Promise((resolve, reject) => {
          handleFileUpload(file, loads[index], errors[index], resolve, reject);
        })
      );
      await Promise.all(uploadPromises); // 모든 파일 업로드가 끝날 때까지 기다림
      fetchExistingFiles(); // 모든 업로드가 끝난 후 한 번만 호출
    } catch (err) {
      console.error("파일 업로드 중 오류 발생:", err);
      openConfirm({
        title: '파일 업로드 실패',
        html: '일부 파일 업로드에 실패했습니다. 다시 시도해주세요.'
      });
    }
  };

  // 파일 업로드 처리 함수
  const handleFileUpload = async (file, load, error, resolve, reject) => {
    const formData = new FormData();
    formData.append(name, file);
    if (jiraTicket) {
      formData.append('jiraTicket', jiraTicket);
    }
    const uploadUrl = `/api/upload/${moduleType}/${moduleItemId}/file`;
    try {
      const response = await axios.post(uploadUrl, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      const uploadedFileData = response.data[0]; // 서버에서 받은 데이터의 첫 번째 요소

      // 성공 콜백 호출
      load(uploadedFileData);
      resolve();
    } catch (err) {
      console.log(err);
      openConfirm({
        title: '처리 중 오류가 발생했습니다.',
        html: `잠시 후 다시 시도해 주세요.`
      });

      error('업로드 실패'); // 실패 처리
      reject(err);
    }
  };

  // 파일 삭제 처리 함수
  const handleFileRemove = async (file) => {
    try {
      // files 배열에서 삭제하려는 파일을 찾음
      const matchedFile = files.find(f => f.source === file.source);

      if (matchedFile && matchedFile.metadata && matchedFile.metadata.uploadId) {
        const uploadId = matchedFile.metadata.uploadId;

        // 파일 삭제 API 호출
        const response = await axios.delete(`/api/upload/${moduleType}/${moduleItemId}/file/${uploadId}`);

        if (response.status === 200) {
          // 상태에서 삭제된 파일 제거
          setFiles(prevFiles => {
            const updatedFiles = prevFiles.filter(item => item.metadata.uploadId !== uploadId);
            return updatedFiles;
          });

          axios.delete(`/api/upload/jira-attachment/${uploadId}`);
        }

      } else {
        console.error("파일의 메타데이터에서 uploadId를 찾을 수 없습니다.");
      }
    } catch (err) {
      console.log(err);

      openConfirm({
        title: '파일 삭제 중 오류가 발생했습니다.',
        html: '잠시 후 다시 시도해 주세요.'
      });
    }
  };

  // 파일 다운로드
  const downloadFile = async (uploadId, fileName) => {
    if (isDownloading) return;
    setIsDownloading(true); // 다운로드 시작 시 상태 변경
    try {
      const response = await axios({
        url: `/api/upload/${moduleType}/${moduleItemId}/file/${uploadId}`,
        method: 'GET',
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error("파일 다운로드 중 오류가 발생했습니다.", error);
      openConfirm({
        title: '파일 다운로드 중 오류가 발생했습니다.',
      });

    } finally {
      setIsDownloading(false); // 2초 후에 다시 다운로드 가능하도록 상태 변경
    }
  };

  return (
    <>
      {isLoading ? (
        <div className="text-center" style={{ height: '100px' }}>
          <Spinner isTable={true} isSmall={true}/>
        </div>
      ) : (
        <StyledFilePond
          files={files}
          // FilePond에서 파일 상태 업데이트 시 호출
          onupdatefiles={(fileItems) => {
            setFiles(prevFiles => {
              // 현재 fileItems에 있는 파일들을 매핑
              const updatedFiles = fileItems.map(fileItem => {
                // 이전에 있는 파일과 새로 추가된 파일을 구분하여 처리
                const existingFile = prevFiles.find(f => f.source === fileItem.source);
                if (existingFile) {
                  // 기존 파일은 그대로 유지
                  return {
                    ...existingFile,
                    ...fileItem,
                  };
                } else {
                  // 새로 추가된 파일은 기본 정보를 설정
                  return {
                    source: fileItem.source,
                    type: fileItem.file?.type || undefined,
                    name: fileItem.file?.name || undefined,
                    size: fileItem.file?.size || undefined,
                    metadata: fileItem.metadata || undefined,
                  };
                }
              });
              return updatedFiles;
            });
          }}
          name={name}
          labelIdle={label ? label : `<div class="font-size-12">
          파일을 이곳에 드래그 & 드롭하거나 클릭하여 첨부하세요<br />
         <span class="text-secondary">최대 ${maxFiles}개 업로드 가능 합니다
         </div>`}
          allowMultiple={true}
          maxFiles={maxFiles}
          allowPaste={false}
          allowRevert={true}
          allowRemove={isDeleteButtonShow}
          allowDrop={allowDrop}
          allowBrowse={allowFileDialog}
          beforeAddFile={(file) => validateFile(file)}

          beforeRemoveFile={(file) => {
            return new Promise((resolve) => {
              // files 배열에서 삭제하려는 파일을 찾음
              const matchedFile = files.find(f => f.source === file.source);

              if (!matchedFile) {
                console.error('파일을 찾을 수 없습니다.');
                openConfirm({
                  title: '파일을 찾을 수 없습니다.',
                  html: '삭제할 파일을 찾을 수 없습니다.'
                });

                resolve(false);  // 삭제 중단
                return;
              }

              // 관리자인 경우 또는 파일을 업로드한 사용자인 경우에만 삭제 허용
              if (isSystemAdmin || isBoardAdmin || matchedFile.metadata.createUserId === userId) {
                openConfirm({
                  title: '파일을 삭제하시겠습니까?',
                  html: '삭제된 파일은 복구할 수 없습니다.',
                  icon: 'trash-circle',
                  callback: async () => {
                    handleFileRemove(file)
                      .then(() => {
                        resolve();  // 삭제 확정
                      })
                      .catch((err) => {
                        console.error(err);
                        openConfirm({
                          title: '삭제 중 오류 발생.',
                          html: '파일 삭제 중 오류가 발생했습니다.'
                        });

                        resolve(false);  // 삭제 중단
                      });
                  }

                });

              } else {
                // 삭제 권한이 없을 경우
                showToast("삭제 권한이 없습니다.", 'error');
                resolve(false);  // 삭제 중단
              }
            });
          }}

          onactivatefile={(file) => {
            // 클릭된 파일의 이름을 가져옵니다.
            const clickedFileName = file.filename;

            // files 배열에서 해당 파일을 찾아 메타데이터를 가져옵니다.
            const matchingFile = files.find(f => f.options && f.options.file && f.options.file.name === clickedFileName);

            if (matchingFile && matchingFile.metadata) {
              const uploadId = matchingFile.metadata.uploadId;

              // 업로드 ID를 사용하여 파일 다운로드 실행
              if (uploadId) {
                downloadFile(uploadId, clickedFileName);
              }
            } else {
              console.log('파일 메타데이터를 찾을 수 없습니다.');
            }
          }}

          server={{
            process: (fieldName, file, metadata, load, error, progress, abort) => {
              pendingFiles.push({ file, load, error });

              // 기존 타이머가 있으면 초기화
              if (uploadTimeout) {
                clearTimeout(uploadTimeout);
              }

              // 일정 시간(100ms) 후에 한번에 업로드 실행
              uploadTimeout = setTimeout(() => {
                const filesToUpload = [...pendingFiles];
                pendingFiles.length = 0; // 리스트 초기화

                if (filesToUpload.length > 0) {
                  handleMultipleFileUpload(
                    filesToUpload.map(f => f.file),
                    filesToUpload.map(f => f.load),
                    filesToUpload.map(f => f.error)
                  );
                }
              }, 100);
            },
          }}
        />
      )}
    </>
  );
}

export default YesMappingFile;
