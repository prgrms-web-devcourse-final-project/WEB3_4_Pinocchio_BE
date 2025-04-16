import {useMutation, useQuery} from "react-query";
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import {useNavigate, Link} from "react-router-dom";
import {Button, Col, Form, Row, Stack} from "react-bootstrap";
import noImage from "../../assets/images/no_image.png";
import {jwtDecode} from "jwt-decode";
import {useState} from "react";
import NotificationModal from "./NotificationModal";
import {buildQuery} from "../../utils/utils";

const fetchUser = async (userId) => {
    const response = await axios.get(`/user/${userId}`);
    return response.data;
};

const UserProfile = ({ page }) => {
    const { openConfirm } = useConfirm();
    const navigate = useNavigate();
    const [isNotificationModalOpen, setNotificationModalOpen] = useState();
    const token = localStorage.getItem('token');
    const loginUser = jwtDecode(token);
    const { isLoading: userLoading, data: user } = useQuery(
        ['fetchUser'],
        () => fetchUser(loginUser.id),
        { keepPreviousData: true, refetchOnWindowFocus: false}
    );


    const deleteMutation = useMutation(() => axios.delete(`/user/${loginUser.id}`), {
        onSuccess: (param) => {
            openConfirm({
                title: "회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다."
                , callback: () => navigate("/login")
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

    const handleDeleteUser = () => {
        openConfirm({
            title: "계정 삭제 안내",
            html: "계정을 정말로 삭제하시겠습니까? 삭제된 계정은 복구할 수 없습니다.",
            callback: () => {
                deleteMutation.mutate()
            }
        })
    }

    const renderButton = () => {
        if (page === "mypage") {
            return <>
                <Button className={"ms-auto"} size={"md"} onClick={() => navigate("/mypage/modify")}>수정</Button>
                <Button size={"md"} onClick={handleDeleteUser}>계정 삭제</Button>
                <span className={"ico-bell cursor-pointer"} onClick={() => setNotificationModalOpen(true)}/>
            </>
        } else if (page === "main") {
            return <Button className={"ms-auto"} size={"md"} onClick={() => navigate("/post/new")}>UPLOAD</Button>
        }
    }

    return (
        <>
            <Form >
                <Row>
                    <Col md={3}>
                        <img src={user?.data.profileImageUrl ? user?.data.profileImageUrl : noImage} style={{ width: "150px", height: "140px" }}/>
                    </Col>
                    <Col >
                        <Stack direction={"horizontal"} gap={3} >
                            <Link to={`/post/list${buildQuery({ type: "users", query: user?.data.nickname })}`}>
                                <h4>{user?.data.nickname}</h4>
                            </Link>
                            {renderButton()}
                        </Stack>
                        <Row>
                            <Col className={"mt-4"}>{user?.data.bio}</Col>
                        </Row>
                    </Col>
                </Row>
            </Form>
            <NotificationModal isOpen={isNotificationModalOpen}
                               onHide={() => setNotificationModalOpen(false)}
            />
        </>
    )
}

export default UserProfile;