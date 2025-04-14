import React, { useRef, useState, useEffect } from 'react';
import profileImage from "../../assets/images/sample_profile.png"
import {Button} from "react-bootstrap";
import axios from "axios";

function ProfileImageZone({ onImageSelect, profileImageUrl }) {
    const fileInputRef = useRef(null);
    const [previewUrl, setPreviewUrl] = useState(profileImage); // 기본 미리보기 이미지


    // 외부에서 profileImageUrl이 변경되었을 때 미리보기 URL 업데이트
    useEffect(() => {
        if (profileImageUrl) {
            setPreviewUrl(profileImageUrl);
        }
    }, [profileImageUrl]);


    const handleButtonClick = () => {
        fileInputRef.current.click(); // 버튼 클릭 시 숨겨진 파일 선택창 열기
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const imageUrl = URL.createObjectURL(file); // 브라우저에서 미리보기용 URL 생성
        setPreviewUrl(imageUrl); // 미리보기 이미지 설정
        onImageSelect(file);     // 부모 컴포넌트로 파일 전달
    };

    return (
        <div>
            <img
                src={previewUrl}
                alt="프로필"
                style={{ width: "100%", objectFit: 'cover' }}
            />
            <br />
            <Button className={"w-100"} onClick={handleButtonClick}>프로필 이미지 변경</Button>

            <input
                type="file"
                accept="image/*"
                ref={fileInputRef}
                style={{ display: 'none' }}
                onChange={handleFileChange}
            />
        </div>
    );
}


export default ProfileImageZone;


