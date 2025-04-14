import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import ProfileImageZone from "./ImageDropZone";
import {useEffect, useState} from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import {useQuery} from "react-query";
import Spinner from "../../shared/Spinner";
import {jwtDecode} from "jwt-decode";
import profileImage from "../../assets/images/sample_profile.png";

const fetchUser = async () => {
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const response = await axios.get(`/user/${loginUser.id}`);
    return response.data;
};

const ProfileEditCard = () => {
    const [email, setEmail] = useState("");
    const [nickname, setNickname] = useState("");
    const [name, setName] = useState("");
    const [bio, setBio] = useState("");
    const [website, setWebsite] = useState("");
    const [isActive, setActive] = useState(true);
    const [imageFile, setImageFile] = useState(null); // 🔥 이미지 파일 상태
    const [profileImageUrl, setProfileImageUrl] = useState(profileImage); // 🔥 이미지 미리보기 상태

    const navigate = useNavigate();

    // 사용자 정보 가져오기 (react-query)
    const { isLoading, data } = useQuery(
        ["ProfileEditCardFetchUser"],
        fetchUser,
        { keepPreviousData: true, refetchOnWindowFocus: false }
    );

    useEffect(() => {
        if (data) {
            const user = data.data;
            setEmail(user.email);
            setNickname(user.nickname);
            setName(user.name);
            setBio(user.bio);
            setWebsite(user.website);
            setActive(user.isActive);
            //  캐시 방지 위해 timestamp 추가
            if (user.profileImageUrl) {
                setProfileImageUrl(`${user.profileImageUrl}?t=${Date.now()}`);
            }
        }
    }, [data]);

    // 수정 버튼 클릭
    const handleClickSubmit = async () => {
        const jsonData = {
            nickname,
            name,
            bio,
            website,
            isActive
        };

        const formData = new FormData();

        // 텍스트 JSON을 multipart 안에 넣기
        formData.append("request", new Blob([JSON.stringify(jsonData)], { type: "application/json" }));

        // 이미지 파일도 함께 업로드
        if (imageFile) formData.append("image", imageFile);

        console.log("보내는 데이터:", jsonData);

        const response = await axios.put("/user", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        });
        // 수정 후 마이페이지로 이동
        navigate("/mypage/like");
    }


    return (
        <Card className="p-5">
            <Card.Body>
                <Row>
                    <Col xs={4} >
                        {/* 이미지 업로드 및 미리보기 */}
                        <ProfileImageZone
                            onImageSelect={(file) => setImageFile(file)}
                            profileImageUrl={profileImageUrl}
                        />
                    </Col>
                    <Col xs={8} >
                        <Form onSubmit={(e) => e.preventDefault()}>
                            <Form.Label>
                                email
                            </Form.Label>
                            <Form.Control
                                plaintext
                                value={email}
                                readOnly
                            />
                            <Form.Label>
                                닉네임
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={nickname}
                                onChange={(e) => setNickname(e.target.value)}
                            />
                            <Form.Label>
                                소개글
                            </Form.Label>
                            <Form.Control
                                as="textarea"
                                style={{ height: 'unset' }}
                                rows={3}
                                value={bio}
                                onChange={(e) => setBio(e.target.value)}
                            />
                            <Form.Label>
                                웹페이지
                            </Form.Label>
                            <Form.Control
                                type="text"
                                value={website}
                                onChange={(e) => setWebsite(e.target.value)}
                            />
                            <Form.Label>
                                계정 공개
                            </Form.Label>
                            <div className="form-check form-switch">
                            <Form.Check
                                type="switch"
                                id="flexSwitchCheckDefault01"
                                name='activateType'
                                onChange={(e) => setActive(e.target.checked)}
                                checked={isActive}
                            />
                            </div>
                        </Form>
                    </Col>
                </Row>
            </Card.Body>
            <Card.Footer className={"mt-5"}>
                <Stack direction={"horizontal"} gap={3} >
                    <Button variant={'primary'} className={"ms-auto"}
                            size={"md"} onClick={handleClickSubmit}>
                        수정
                    </Button>
                    <Button variant={'secondary'} size={"md"} onClick={() => navigate("/mypage/like")}>

                    취소
                    </Button>
                </Stack>
            </Card.Footer>
            <Spinner show={isLoading} />
        </Card>
    )
}

export default ProfileEditCard;