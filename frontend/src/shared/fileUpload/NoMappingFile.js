import React, { useState, useEffect } from 'react';
import { FilePond } from 'react-filepond';
import styled from 'styled-components';
import 'filepond/dist/filepond.min.css';
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import {useToast} from "../../hooks/useToast";

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
        color: #6A9C89;
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
  껍데기가 없다 경우에 사용하는 파일 업로드 컴포넌트 입니다.
  껍데기가 없다 = 게시물을 만들 때에 파일 업로드도 함께 진행 -> 즉, '해당' 게시물의 매핑할 수 없다 
  ex) 게시판 등록
*/

function NoMappingFile({
  label,            // 파일 업로드 안내 문구
  name = "files",   // 백엔드 컨트롤러에서 받는 파일의 이름
  maxFiles = 3,     // 최대 업로드 가능한 파일 개수
  onUploadComplete, // 업로드 완료 후 부모 컴포넌트로 전달할 콜백 함수
}) {
  const { openConfirm } = useConfirm();
  const showToast = useToast();

  // 파일 목록 및 데이터를 통합 관리하는 상태 변수
  const [files, setFiles] = useState([]);

  // 업로드 대기 중인 파일 저장
  const pendingFiles = [];
  let uploadTimeout = null;

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
      // 파일이 유효하지 않을 때 알림
        openConfirm({
            title: '파일 정보가 유효하지 않습니다.',
            html: "유효한 파일을 선택해주세요."
        });
      return false;
    }

    if (validationInfo.maxFileSize === 0 || validationInfo.allowedExtensions.length === 0) {
      // validationInfo가 아직 설정되지 않았을 때 경고
      console.warn("파일 검증이 유효한 상태가 아닙니다.");
      return false;
    }

    const file = fileItem.file;
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

  // 파일 목록 상태 업데이트 시마다 부모 컴포넌트로 데이터 전달 및 콘솔 출력
  useEffect(() => {
    const uploadedFilesData = files.map(fileItem => fileItem.data);
    onUploadComplete(uploadedFilesData);
  }, [files]);

  // 파일 일괄 업로드 함수
  const handleMultipleFileUpload  = async (files, loads, errors) => {
    try {
      const uploadPromises = files.map((file, index) =>
          new Promise((resolve, reject) => {
            handleFileUpload(file, loads[index], errors[index], resolve, reject);
          })
      );
      await Promise.all(uploadPromises); // 모든 파일 업로드가 끝날 때까지 기다림
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
    const uploadUrl = '/api/upload/temp';
    try {
      const response = await axios.post(uploadUrl, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      // 서버에서 반환된 고유 파일 ID를 가져옴
      const uniqueFileId = response.data[0].uploadTempId; // 서버에서 반환된 배열의 첫 번째 요소의 고유 ID
      if (uniqueFileId) {
        load(uniqueFileId); // FilePond에 고유 식별자를 알림

        // 파일의 메타데이터에 uniqueFileId 저장
        file.metadata = { uniqueFileId };

        // 파일 업로드 데이터 상태 업데이트
        setFiles(prevFiles => {
          const newFiles = [...prevFiles];
          const index = newFiles.findIndex(f => f.file === file);

          if (index !== -1) {
            // 기존 파일을 업데이트
            newFiles[index] = { file, metadata: { uniqueFileId }, data: response.data[0] };
          } else {
            // 새 파일을 추가
            newFiles.push({ file, metadata: { uniqueFileId }, data: response.data[0] });
          }
          resolve();
          return newFiles;
        });
      } else {
        throw new Error("uniqueFileId is undefined");
      }
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
      //상태에서 삭제된 파일 제거
      setFiles(prevFiles => {
        const updatedFiles = prevFiles.filter(item => item.metadata.uniqueFileId !== file.metadata.uniqueFileId);
        return updatedFiles;
      });
    } catch (err) {
      console.log(err);
        openConfirm({
            title: '파일 삭제 중 오류가 발생했습니다.',
            html: `잠시 후 다시 시도해 주세요.`
        });
    }
  };

  // 모든 파일 업로드가 완료되면 호출되는 함수
  const handleProcessAllComplete = () => {
    if (files.length > 0) {
      // 모든 파일 업로드가 완료되면 부모 컴포넌트로 데이터와 파일 객체를 함께 전달
      onUploadComplete(files.map(item => ({
        uploadTempId: item.metadata.uniqueFileId,  // uniqueFileId 값을 명시적으로 전달
        ...item.data,  // 추가 데이터가 있으면 함께 전달
        file: item.file // 파일 객체를 추가
      })));
    }
  };

  return (
    <>
      <StyledFilePond
        files={files.map(fileItem => fileItem.file)} // 조건에 따라 파일 리스트 전달
        // FilePond에서 파일 상태 업데이트 시 호출
        onupdatefiles={(fileItems) => {
          if (validationInfo.maxFileSize === 0 || validationInfo.allowedExtensions.length === 0) {
            return; // validationInfo가 유효하지 않으면 검증하지 않고 종료
          }
          const validFiles = fileItems.filter(fileItem => validateFile(fileItem));

          setFiles(prevFiles => {
            return validFiles.map(fileItem => {
              const existingFile = prevFiles.find(f => f.file === fileItem.file);
              if (existingFile) {
                return { ...existingFile, file: fileItem.file };
              } else {
                return { file: fileItem.file, metadata: fileItem.file.metadata };
              }
            });
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
        beforeAddFile={(file) => validateFile(file)}
        onprocessfiles={() => handleProcessAllComplete()}
        beforeRemoveFile
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
            // 파일 업로드 처리 함수 호출
            //return handleFileUpload(file, load, error, progress, abort);
          },
          revert: (uniqueFileId, load) => {
            // uniqueFileId로 삭제할 파일 찾기
            const fileToRemove = files.find(
              (f) => String(f.metadata?.uniqueFileId) === String(uniqueFileId) // 비교를 명확히
            );
          
            if (fileToRemove) {
              // 상태에서 파일 제거
              setFiles((prevFiles) =>
                prevFiles.filter(
                  (item) =>
                    String(item.metadata?.uniqueFileId) !== String(uniqueFileId) // 비교 조건 유지
                )
              );
          
              // handleFileRemove 호출
              handleFileRemove(fileToRemove);
            } else {
              console.warn(`파일을 찾을 수 없음: uniqueFileId = ${uniqueFileId}`);
            }
          
            load(); // FilePond에 삭제 완료 알림
          },
          
        }}
        maxParallelUploads={maxFiles}
        credits={false}
      />
    </>
  );
}

export default NoMappingFile;
