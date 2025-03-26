import React, { useState, useEffect } from 'react';
import { FilePond } from 'react-filepond';
import styled from 'styled-components';
import 'filepond/dist/filepond.min.css';
import axios from "axios";
import Alert from "../Alert"

const StyledFilePond = styled(FilePond)`
  .filepond--panel-root {
    background-color: #043a5a;
    color: #ffffff;
  }
  .filepond--drop-label {
    color: #ffffff;
  }
  .filepond--label-action {
    color: #ffffff;
  }
  .filepond--item-panel {
    background-color: #33475b;
    color: #ffffff;
  }
    .filepond--file-info {
    color: #1e90ff; /* 파일명 색상 변경 */
    text-decoration: underline; /* 링크처럼 보이게 밑줄 추가 */
    cursor: pointer !important; /* 강제 적용 */
    pointer-events: auto !important; /* 포인터 이벤트 허용 */
`;

function ViewingFile({ moduleType, moduleItemId }) {
  const [files, setFiles] = useState([]);
  const [isDownloading, setIsDownloading] = useState(false); // 다운로드 상태 관리

  const fetchExistingFiles = async () => {
    try {
      const response = await axios.get(`/api/upload/${moduleType}/${moduleItemId}/file/metadata`);
      const existingFiles = response.data;

      if (existingFiles && existingFiles.length > 0) {
        const filePondFiles = existingFiles.map(file => ({
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
          },
          key: file.uploadId, 
        }));
        setFiles(filePondFiles);
      }
    } catch (err) {
      console.log(err);
      Alert({
        title: '파일 정보를 불러오는 중 오류가 발생했습니다.',
        html: '잠시 후 다시 시도해 주세요.',
        icon: 'warning',
        confirmButtonText: '확인',
      });
    }
  };

  useEffect(() => {
    fetchExistingFiles();
  }, [moduleType, moduleItemId]);

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
      Alert({
        title: '파일 다운로드 중 오류가 발생했습니다.',
        icon: 'warning',
        confirmButtonText: '확인',
      });
    } finally {
      setTimeout(() => {
        setIsDownloading(false); // 2초 후에 다시 다운로드 가능하도록 상태 변경
      }, 2000); // 2초 딜레이
    }
  };

  return (
    <>
      {files.length === 0 ? (
        <div
          style={{
            backgroundColor: '#043a5a', // 배경색
            color: '#ffffff', // 텍스트 색상
            borderRadius: '10px', // 둥근 모서리
            padding: '20px', // 패딩
            fontSize: '12px', // 폰트 크기
            textAlign: 'center', // 텍스트 가운데 정렬
            margin: '20px auto', // 상하 마진과 가운데 정렬
          }}
        >
          파일이 없습니다
        </div>
      ) : (
        <StyledFilePond
          files={files}
          onupdatefiles={() => { }}
          allowReplace={false}
          allowRemove={false}
          allowReorder={false}
          allowRevert={false}
          instantUpload={false}
          allowMultiple={true}
          allowPaste={false}
          maxFiles={files.length}
          server={null} // 서버와의 통신 비활성화
          itemInsertLocation="after" // 아이템을 그대로 표시
          onactivatefile={(file, event) => {
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
        />
      )}
    </>
  );
}

export default ViewingFile;
