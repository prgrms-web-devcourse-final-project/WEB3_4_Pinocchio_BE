import logo from "../../assets/images/pinocchio.png"
import {Link, useLocation, useNavigate} from "react-router-dom";
import useConfirm from "../../hooks/useConfirm";
import {Button} from "react-bootstrap";
import axios from "axios";
import {useEffect, useState} from "react";
import Spinner from "../../shared/Spinner";
import BoardSearch from "../../pages/share/BoardSearch";


const Header = () => {
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();
    const { openConfirm } = useConfirm();
    const { pathname } = useLocation();

    const handleLogoutClick = async () => {
        try {
            setIsLoading(true);
            await axios.post("/auth/logout", {});
            localStorage.removeItem('loginUser');
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
                        <Link to={"/post/list"}>
                            <img src={logo} style={{ width: "120px" }} alt="pinocchio"/>
                        </Link>
                    </div>
                    <BoardSearch type={'notice'} />
                    <Button size={"md"} onClick={handleLogoutClick}>LOGOUT</Button>
                    {!pathname.includes("/board/mypage") && <Button size={"md"} onClick={() => navigate("/mypage/like")}>MYPAGE</Button>}
                </div>
            </div>
            <Spinner show={isLoading}/>
        </header>
    );
}

export default Header;