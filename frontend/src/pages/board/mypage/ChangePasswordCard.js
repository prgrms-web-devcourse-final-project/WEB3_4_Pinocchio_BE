import {Button, Card, Col, Form, Row, Stack} from "react-bootstrap";
import {useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import {isEmptyOrNull} from "../../../utils/utils";
import useConfirm from "../../../hooks/useConfirm";

const ChangePasswordCard = () => {
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const navigate = useNavigate();
    const { openConfirm } = useConfirm();

    const handleClickSubmit = async () => {
        if (isEmptyOrNull(oldPassword) || isEmptyOrNull(newPassword)) {
            openConfirm({
                title: '필수 항목이 없습니다.',
            });
            return;
        }
        const params = {
            currentPassword: oldPassword
            , newPasswordConfirm: newPassword
        }
        console.log('parmas: ', params);
        const response = await axios.put("/user/password", params);
        console.log(response);
        navigate("/board/mypage/like");
    }

    return (
        <Card className="p-5">
            <Card.Body>
                <Row>
                    <Col xs={4} />
                    <Col xs={8} >
                        <Form onSubmit={(e) => e.preventDefault()}>
                            <Form.Label>
                                기존 비밀번호
                            </Form.Label>
                            <Form.Control
                                type="password"
                                value={oldPassword}
                                onChange={(e) => setOldPassword(e.target.value)}
                            />
                            <Form.Label>
                                비밀번호 변경
                            </Form.Label>
                            <Form.Control
                                type="password"
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                            />
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
                    <Button variant={'secondary'} size={"md"} onClick={() => navigate("/board/mypage/like")}>
                        취소
                    </Button>
                </Stack>
            </Card.Footer>
        </Card>
    );
}

export default ChangePasswordCard;