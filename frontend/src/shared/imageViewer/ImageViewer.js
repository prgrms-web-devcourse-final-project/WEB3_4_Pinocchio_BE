import Lightbox from "yet-another-react-lightbox";
import "yet-another-react-lightbox/styles.css";
import Fullscreen from "yet-another-react-lightbox/plugins/fullscreen";
import Zoom from "yet-another-react-lightbox/plugins/zoom";
import Thumbnails from "yet-another-react-lightbox/plugins/thumbnails";
import Download from "yet-another-react-lightbox/plugins/download";
import "yet-another-react-lightbox/plugins/thumbnails.css";
import useConfirm from "../../hooks/useConfirm";
const ImageViewer = ({isOpen, close, slideData, currentIndex}) => {
    const { openConfirm } = useConfirm();

    // 파일 다운로드
    const handleDownload = async ({ slide }) => {
        try {
            const response = await fetch(slide.src);
            const blob = await response.blob();

            // 파일명을 slide.title 값으로 설정
            const filename = slide.title || "downloaded-file";

            // Blob을 다운로드할 링크 생성
            const link = document.createElement("a");
            link.href = URL.createObjectURL(blob);
            link.download = filename; // 파일명 설정
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        } catch (error) {
            console.error("error handleDownload:", error);
            openConfirm({
                title: '다운로드에 실패하였습니다.',
                html: error.response?.data?.message || '에러: 관리자에게 문의 바랍니다.'
            });
        }
    };

    return (
        <Lightbox
            open={isOpen}
            close={close}
            slides={slideData}
            plugins={[Fullscreen, Thumbnails, Zoom, Download]}
            zoom={{ maxZoomPixelRatio: 4 }}
            download={{ download: handleDownload }}
            index={currentIndex}
        />
    )
}
export default ImageViewer