import {Button, Form, Modal} from "react-bootstrap";
import {useState} from "react";
import axios from "axios";
import {useMutation} from "react-query";
import useConfirm from "../../hooks/useConfirm";

const PasswordResetModal = ({isOpen, onHide}) => {
    const [email, setEmail] = useState('');
    const {openConfirm} = useConfirm();
    const mutation = useMutation((sendData) => axios.post('/user/password/reset', sendData), {
        onSuccess: (response) => {
            openConfirm({
                title: response.message
                , showCancelButton: false
            });
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });
    const handleConfirmClick = () => {
        const sendData = {email};
        mutation.mutate(sendData);
    }

    return (
        <Modal show={isOpen}
               centered
               backdrop="static" // 백그라운드 클릭 시 닫히지 않도록
               scrollable>
            <Modal.Header closeButton onHide={onHide}>등록한 이메일 주소</Modal.Header>
            <Modal.Body>
                <Form onSubmit={e => e.preventDefault()}>
                    <Form.Label className="text-secondary">가입 시 등록했던 이메일 주소를 입력해주세요.</Form.Label>
                    <Form.Control
                        type="text"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </Form>
            </Modal.Body>
            <Modal.Footer>
                <Button onClick={handleConfirmClick}>확인</Button>
            </Modal.Footer>
        </Modal>
    );
}

export default PasswordResetModal;