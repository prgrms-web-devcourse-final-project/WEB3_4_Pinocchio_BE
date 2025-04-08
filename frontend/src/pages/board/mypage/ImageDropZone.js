import React, { useRef, useState } from 'react';
import profileImage from "../../../assets/images/sample_profile.png"
import {Button} from "react-bootstrap";

function ProfileImageZone() {
    const fileInputRef = useRef(null);
    const [previewUrl, setPreviewUrl] = useState(profileImage);

    const handleButtonClick = () => {
        fileInputRef.current.click();
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const imageUrl = URL.createObjectURL(file);
        setPreviewUrl(imageUrl);

        // 여기서 file은 서버 업로드용으로 저장해둘 수 있음
        console.log('선택한 파일:', file);
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


