import logoLogin from "../../assets/images/pinocchio.png";
import kakaoLogin from "../../assets/images/kakao_login.png"
import googleLogin from "../../assets/images/google_login.png"
import {useState} from "react";
import {Link, useLocation, useNavigate} from "react-router-dom";
import {isEmptyOrNull, useEnterKeySubmit} from "../../utils/utils";
import Spinner from "../../shared/Spinner";
import useConfirm from "../../hooks/useConfirm";
import axios from "axios";
import {Stack} from "react-bootstrap";

const Login = () => {
    const navigate = useNavigate();
    const { openConfirm } = useConfirm();
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleClickLogin = () => {
        let sendData = undefined;

        if (isEmptyOrNull(loginId)) {
            openConfirm({
                title: '로그인 중 오류가 발생했습니다.',
                html: '사용자 계정을 입력하세요'
            });
            return false;
        }

        if (isEmptyOrNull(password)) {
            openConfirm({
                title: '로그인 중 오류가 발생했습니다.',
                html: '사용자 비밀번호를 입력하세요'
            });
            return false;
        }

        sendData = {email : loginId, password};
        requestLogin(sendData);
    }

    const requestLogin = async (sendData) => {
        try {
            setIsLoading(true);
            const response = await axios.post('/auth/login', sendData);
            // localStorage.setItem("token", response.data);
            navigate('/board/list')
        } catch (error) {
            console.log("error login api: ", error);
            openConfirm({
                title: '데이터를 불러오는 중 오류가 발생했습니다.',
                html: error.response?.data?.message || "에러: 관리자에게 문의바랍니다."
            });
        }
        finally {
            setIsLoading(false);
        }
    }

    // 엔터 키를 눌렀을 때 로그인 버튼 클릭 동작을 위한 훅
    const handleEnterKey = useEnterKeySubmit(handleClickLogin);

    return (
        <>
            <div className="kw-login">
                <div className="kw-login-form">
                    <div className="kw-login-logo">
                        <img src={logoLogin} style={{ width: "400px" }} alt="pinocchio"/>
                    </div>
                    <div className="kw-login-input">
                        <input
                            type="text"
                            className="form-control form-control-lg"
                            placeholder="이메일를 입력하세요"
                            value={email}
                            onChange={(event) => setLoginId(event.target.value)}
                            onKeyDown={handleEnterKey}
                        />
                        <input
                            type="password"
                            className="form-control form-control-lg"
                            placeholder="비밀번호를 입력하세요"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            onKeyDown={handleEnterKey}
                        />
                    </div>
                    <div className="kw-login-button">
                        <button type="button" className="btn btn-primary btn-lg" onClick={handleClickLogin}>로그인</button>
                    </div>
                    <div className="kw-login-text d-flex justify-content-between">
                        <Link to={"#"}><img src={kakaoLogin} style={{ width: "200px", height: "50px" }}/></Link>
                        <Link to={"#"}><img src={googleLogin} style={{ width: "200px", height: "50px" }}/></Link>
                    </div>
                    <div className="kw-login-text">
                        <Stack direction={"horizontal"}>
                            <Link to={"/signup"}>회원 가입</Link>
                            <p className={"ms-auto"}>비밀번호 찾기</p>
                        </Stack>
                    </div>
                </div>
            </div>
            <Spinner show={isLoading}/>
        </>
    );
}

export default Login;