import React from 'react';
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";

const FileDownloadAsZip = ({ moduleType, moduleItemId, propFileName }) => {
    const { openConfirm } = useConfirm();
    const downloadFile = async () => {
        try {
            const response = await axios({
                url: `/api/upload/${moduleType}/${moduleItemId}/file`,
                method: 'GET',
                responseType: 'blob',
            });

            // 파일명 추출
            const contentDisposition = response.headers['content-disposition'];
            let fileName = 'downloadedFile.zip'; // 기본 파일명 설정
            if (contentDisposition) {
                const matches = contentDisposition.match(/filename\*=UTF-8''([^;]*)/);
                if (matches != null && matches[1]) {
                    fileName = decodeURIComponent(matches[1]); // UTF-8로 인코딩된 파일명을 디코딩
                }
            }

            // Prop으로 받은 파일명을 우선적으로 사용하고, 없으면 추출한 파일명을 사용
            if (propFileName) {
                fileName = propFileName.endsWith('.zip') ? propFileName : `${propFileName}.zip`;
            }

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', fileName); // 최종 파일명 사용
            document.body.appendChild(link);
            link.click();
            link.remove();
        } catch (error) {
            console.error("파일 다운로드 중 오류가 발생했습니다.", error);

        }
    };

    return (
        <span>
            <i className="mdi mdi-file-link text-primary font-size-20" style={{ cursor: "pointer" }} onClick={() => { downloadFile() }} />
        </span>
    );

};

export default FileDownloadAsZip;
