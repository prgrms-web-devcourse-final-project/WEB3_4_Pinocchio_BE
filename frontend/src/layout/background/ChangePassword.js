import {Button, Col, Form, Modal} from "react-bootstrap";
import styles from "./ChangePassword.module.scss";
import {useEffect, useState} from "react";
import {useToast} from "../../hooks/useToast";
import {useEnterKeySubmit} from "../../utils/utils";

const ChangePassword = ({ isOpen, onHide, onSave }) => {
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (!isOpen) {
            setCurrentPassword('');
            setNewPassword('');
            setConfirmNewPassword('');
            setErrors({});
        }
    }, [isOpen]);

    const handleSave = () => {
        let errors = {};
        // 유효성 검사
        if (!currentPassword) {
            errors.currentPassword = '기존 비밀번호를 입력해주세요.';
        }

        if (!newPassword) {
            errors.newPassword = '새 비밀번호를 입력해주세요.';
        } else if (newPassword.length < 8) {
            errors.newPassword = '새 비밀번호는 최소 8자 이상이어야 합니다.';
        }

        if (!confirmNewPassword) {
            errors.confirmNewPassword = '새 비밀번호를 다시 입력해주세요.';
        } else if (newPassword !== confirmNewPassword) {
            errors.confirmNewPassword = '새 비밀번호가 일치하지 않습니다.';
        }

        setErrors(errors);

        if (Object.keys(errors).length === 0) {
            const sendData = {
                currentPassword,
                newPassword,
                confirmNewPassword
            };
            onSave(sendData);

            // 필드값 초기화
            setCurrentPassword('');
            setNewPassword('');
            setConfirmNewPassword('');
        }
    };

    // 엔터 키를 눌렀을 때 저장 버튼 클릭 동작을 위한 훅
    const handleEnterKey = useEnterKeySubmit(handleSave);

    return (
        <Modal
            show={isOpen}
            centered
            backdrop="static" // 백그라운드 클릭 시 닫히지 않도록
            dialogClassName={styles.customDialog}
            contentClassName={styles.customContent}
        >
            <Modal.Header closeButton onHide={onHide}>
                <Modal.Title>
                    <h5>비밀번호 변경</h5>
                </Modal.Title>
            </Modal.Header>
            <Modal.Body>
                <Form className="row gap-4">
                    <Col xs={12}>
                        <Form.Label>
                            기존 비밀번호
                            <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="password"
                            placeholder="기존 비밀번호를 입력해주세요"
                            value={currentPassword}
                            onChange={(e) => setCurrentPassword(e.target.value)}
                            isInvalid={!!errors.currentPassword}
                            required
                            onKeyDown={handleEnterKey}
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.currentPassword}
                        </Form.Control.Feedback>
                    </Col>
                    <Col xs={12}>
                        <Form.Label>
                            새 비밀번호
                            <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="password"
                            placeholder="새 비밀번호를 입력해주세요"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            isInvalid={!!errors.newPassword}
                            onKeyDown={handleEnterKey}
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.newPassword}
                        </Form.Control.Feedback>
                    </Col>
                    <Col xs={12}>
                        <Form.Label>
                            새 비밀번호 다시 입력
                            <span className="text-danger">*</span>
                        </Form.Label>
                        <Form.Control
                            type="password"
                            placeholder="새 비밀번호를 다시 입력해주세요"
                            value={confirmNewPassword}
                            onChange={(e) => setConfirmNewPassword(e.target.value)}
                            isInvalid={!!errors.confirmNewPassword}
                            onKeyDown={handleEnterKey}
                        />
                        <Form.Control.Feedback type="invalid">
                            {errors.confirmNewPassword}
                        </Form.Control.Feedback>
                    </Col>
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button variant={"primary"} onClick={onHide}>
                    닫기
                </Button>
                <Button
                    variant={"secondary"}
                    onClick={handleSave}
                >
                    저장
                </Button>
            </Modal.Footer>
        </Modal>
    );
}

export default ChangePassword;