import logo from "../../assets/images/pinocchio.png"
import {Link, useNavigate} from "react-router-dom";
import useConfirm from "../../hooks/useConfirm";
import BoardSearch from "../../pages/board/share/BoardSearch";
import {Button} from "react-bootstrap";
import ChangePassword from "./ChangePassword";
import axios from "axios";
import {useToast} from "../../hooks/useToast";
import {useEffect, useState} from "react";
import Spinner from "../../shared/Spinner";


const Header = () => {
    const [loginUser, setLoginUser] = useState({});
    const showToast = useToast();
    const [isLoading, setIsLoading] = useState(false);
    const [isChangePasswordModalOpen, setChangePasswordModalOpen] = useState(false);
    const navigate = useNavigate();
    const { openConfirm } = useConfirm();


    const handlePasswordChangeSave = async (data) => {
        try {
            setIsLoading(true);
            const response = await axios.post('/api/change-password', {
                userId: loginUser.userId,
                currentPassword: data.currentPassword,
                newPassword: data.newPassword,
                confirmPassword: data.confirmPassword,
            });

            if (response.data.result === 'success') {
                showToast('성공', '비밀번호가 성공적으로 변경되었습니다.');
                setChangePasswordModalOpen(false);
            } else {
                showToast('실패', response.data.message);
            }
        } catch (error) {
            console.error('Error changing password', error);
            showToast('실패', error.response.data.message);
        } finally {
            setIsLoading(false); // 다운로드 완료 또는 오류 시 스피너 비활성화
        }
    };

    const handleLogoutClick = async () => {
        try {
            setIsLoading(true);
            await axios.post("/auth/logout", {});
            navigate("/login");
        } catch (error) {
            console.log("error login api: ", error);
            openConfirm({
                title: '데이터를 불러오는 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <header className="kw-header">
            <div className="kw-inner">
                <div className="kw-header-gnb">
                    <div className="kw-header-gnb-brand">
                        <Link to={"/board/list"}>
                            <img src={logo} style={{ width: "120px" }} alt="pinocchio"/>
                        </Link>
                    </div>
                    <BoardSearch type={'notice'} />
                    <Button size={"md"} onClick={handleLogoutClick}>LOGOUT</Button>
                    <Button size={"md"} onClick={() => navigate("/board/mypage")}>MYPAGE</Button>
                    <ChangePassword isOpen={isChangePasswordModalOpen}
                                    onHide={() => setChangePasswordModalOpen(false)}
                                    onSave={handlePasswordChangeSave}
                    />
                </div>
            </div>
            <Spinner show={isLoading}/>
        </header>
    );
}

export default Header;