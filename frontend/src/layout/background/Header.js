import logo from "../../assets/images/pinocchio.png"
import {Link, useLocation, useNavigate} from "react-router-dom";
import useConfirm from "../../hooks/useConfirm";
import {Button} from "react-bootstrap";
import axios from "axios";
import {useEffect, useState} from "react";
import Spinner from "../../shared/Spinner";
import BoardSearch from "../../pages/share/BoardSearch";
import {useMutation} from "react-query";


const Header = () => {
    const navigate = useNavigate();
    const { openConfirm } = useConfirm();
    const { pathname } = useLocation();

    const handleLogoutClick = async () => {
        openConfirm({
            title: "로그아웃 하시겠습니까?",
            callback: () => {}//logoutMutation.mutate()
        })
    }

    const logoutMutation = useMutation(axios.post("/auth/logout", {}), {
        onSuccess: (param) => {
            localStorage.removeItem('token');
            openConfirm({
                title: "로그아웃 되었습니다."
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

    return (
        <header className="kw-header">
            <div className="kw-inner">
                <div className="kw-header-gnb">
                    <div className="kw-header-gnb-brand">
                        <Link to={"/post/list"}>
                            <img src={logo} style={{ width: "120px" }} alt="pinocchio"/>
                        </Link>
                    </div>
                    <BoardSearch type={'notice'} />
                    <Button size={"md"} onClick={handleLogoutClick}>LOGOUT</Button>
                    {!pathname.includes("/board/mypage") && <Button size={"md"} onClick={() => navigate("/mypage/like")}>MYPAGE</Button>}
                </div>
            </div>
            <Spinner show={logoutMutation.isLoading}/>
        </header>
    );
}

export default Header;