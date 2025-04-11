import {Button, Form, Modal, Stack} from "react-bootstrap";
import {useMutation, useQuery} from "react-query";

import axios from "axios";
import useConfirm from "../../../hooks/useConfirm";

const fetchNotification = async () => {
    const response = await axios.get(`/notifications/settings`);
    return response.data.data;
}

const NotificationModal = ({ isOpen, onHide }) => {
    const {openConfirm} = useConfirm();

    const { isLoading, data, refetch } = useQuery(
        ['fetchNotification'],
        () => fetchNotification(),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );

    const mutation = useMutation((sendData) => axios.put('/notifications/settings', sendData), {
        onSuccess: (response) => {
            refetch()
        }
        , onError: (error) => {
            openConfirm({
                title: '처리 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
    });

    const handleSwitchToggle = (targetName) => {
        const newData = {...data};
        newData[targetName] = !newData[targetName]
        mutation.mutate(newData);
    }

    return (
        <Modal show={isOpen}
               centered
               backdrop="static" // 백그라운드 클릭 시 닫히지 않도록
               scrollable>
            <Modal.Header closeButton onHide={onHide}>알림 설정</Modal.Header>
            <Modal.Body>
                <Form onSubmit={e => e.preventDefault()}>
                    <Stack direction="horizontal" gap={3}>
                        <h5>DM</h5>
                        <Form.Check
                            className={"p-2 ms-auto"}
                            type="switch"
                            checked={data?.message}
                            onChange={() => handleSwitchToggle('message')}
                        />
                    </Stack>
                    <Stack direction="horizontal" gap={3}>
                        <h5>좋아요</h5>
                        <Form.Check
                            className={"p-2 ms-auto"}
                            type="switch"
                            checked={data?.like}
                            onChange={() => handleSwitchToggle('like')}
                        />
                    </Stack>
                    <Stack direction="horizontal" gap={3}>
                        <h5>댓글</h5>
                        <Form.Check
                            className={"p-2 ms-auto"}
                            type="switch"
                            checked={data?.comment}
                            onChange={() => handleSwitchToggle('comment')}
                        />
                    </Stack>
                    <Stack direction="horizontal" gap={3}>
                        <h5>팔로우</h5>
                        <Form.Check
                            className={"p-2 ms-auto"}
                            type="switch"
                            checked={data?.follow}
                            onChange={() => handleSwitchToggle('follow')}
                        />
                    </Stack>
                    <Stack direction="horizontal" gap={3}>
                        <h5>멘션</h5>
                        <Form.Check
                            className={"p-2 ms-auto"}
                            type="switch"
                            checked={data?.mention}
                            onChange={() => handleSwitchToggle('mention')}
                        />
                    </Stack>
                </Form>
            </Modal.Body>
        </Modal>
    );
}

export default NotificationModal;