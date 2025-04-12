import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import ProfileImageZone from "./ImageDropZone";
import {useEffect, useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import {useQuery} from "react-query";
import Spinner from "../../shared/Spinner";
import {jwtDecode} from "jwt-decode";

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
    const navigate = useNavigate();
    const userId = 1;
    const { isLoading, data } = useQuery(
        ['ProfileEditCardFetchUser'],
        () => fetchUser(userId),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    useEffect(() => {
        if (data) {
            const user = data.data;
            setEmail(user.email);
            setNickname(user.nickname);
            setName(user.name);
            setBio(user.bio);
            setWebsite(user.website);
            setActive(user.isActive)
        }
    }, [data])

    const handleClickSubmit = async () => {
        const parmas = {
            nickname
            , name
            , bio
            , website
            , isActive
            , profileImageUrl: ""
        }
        console.log('params: ', parmas)
        const response = await axios.put("/user", parmas);
        navigate("/mypage/like");
    }
    return (
        <Card className="p-5">
            <Card.Body>
                <Row>
                    <Col xs={4} >
                        <ProfileImageZone />
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