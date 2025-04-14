import React, { useRef, useState } from 'react';
import profileImage from "../../assets/images/sample_profile.png"
import {Button} from "react-bootstrap";
import axios from "axios";

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

        const jsonData = {
            "name": "홍길동",
            "nickname": "gildong",
            "bio": "안녕하세요!",
            "website": "https://example.com",
            "profileImageUrl": null,
            "isActive": true
        }
        // json 데이터 삽입
        const formData = new FormData();
        formData.append("request", new Blob([JSON.stringify(jsonData)], {
            type: "application/json",
        }));
        // file 데이터 삽입
        formData.append("image", file);
        // 여기서 file은 서버 업로드용으로 저장해둘 수 있음

        // 수정 요청
        axios.put("/user", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        }).then((response) => {
            console.log(response);
        }).catch((error) => {
            console.log(error);
        });
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


