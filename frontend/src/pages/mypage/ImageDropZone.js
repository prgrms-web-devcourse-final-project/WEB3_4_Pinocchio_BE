import React, {useEffect, useRef, useState} from 'react';
import noImage from "../../assets/images/no_image.png"
import {Button} from "react-bootstrap";
import axios from "axios";

function ProfileImageZone({ profileImageUrl, handleProfileImageChange }) {
    const fileInputRef = useRef(null);
    const [previewUrl, setPreviewUrl] = useState();

    useEffect(() => {
        if (profileImageUrl) {
            setPreviewUrl(profileImageUrl)
        }
    }, [profileImageUrl])

    const handleButtonClick = () => {
        fileInputRef.current.click();
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (!file) return;

        const imageUrl = URL.createObjectURL(file);
        setPreviewUrl(imageUrl);
        handleProfileImageChange(file)
    };

    return (
        <div>
            <img
                src={previewUrl ? previewUrl : noImage}
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


