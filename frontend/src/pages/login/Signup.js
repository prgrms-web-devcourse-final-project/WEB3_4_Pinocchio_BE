import logoLogin from "../../assets/images/pinocchio.png";
import {Link, useNavigate} from "react-router-dom";
import kakaoLogin from "../../assets/images/kakao_login.png";
import googleLogin from "../../assets/images/google_login.png";
import {Stack} from "react-bootstrap";
import Spinner from "../../shared/Spinner";
import axios from "axios";
import useConfirm from "../../hooks/useConfirm";
import {isEmptyOrNull, useEnterKeySubmit} from "../../utils/utils";
import {useState} from "react";

const Signup = () => {
    const navigate = useNavigate();
    const { openConfirm } = useConfirm();
    const [loginId, setLoginId] = useState("");
    const [password, setPassword] = useState("");
    const [nickname, setNickName] = useState("");
    const [name, setName] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    const handleClickLogin = () => {
        if (isEmptyOrNull(loginId)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '사용자 계정을 입력하세요'
            });
            return false;
        }

        if (isEmptyOrNull(password)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '사용자 비밀번호를 입력하세요'
            });
            return false;
        }

        if (isEmptyOrNull(nickname)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '사용자 닉네임을 입력하세요'
            });
            return false;
        }

        if (isEmptyOrNull(name)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '사용자 이름을 입력하세요'
            });
            return false;
        }

        const sendData = {
            email : loginId
            , password
            , nickname
            , name
        };
        requestSignup(sendData);
    }

    const requestSignup = async (sendData) => {
        try {
            setIsLoading(true);
            const response = await axios.post('/auth/signup', sendData);
            // 회원가입 성공 → 로그인 페이지로 이동
            navigate('/login');
        } catch (error) {
            console.log("error login api: ", error);

            // 백엔드에서 내려준 유효성 검증 오류 포함 응답 처리
            const errorResponse = error.response?.data;
            const baseMessage = errorResponse?.message || "에러: 관리자에게 문의바랍니다.";
            const fieldErrors = errorResponse?.errors;

            let html = baseMessage;

            // 필드별 오류 메시지 모아 보여주기
            if (fieldErrors && typeof fieldErrors === "object") {
                html += "<br/><br/>";
                for (const [field, message] of Object.entries(fieldErrors)) {
                    html += `<b>${field}</b>: ${message}<br/>`;
                }
            }

            openConfirm({
                title: '회원가입 오류',
                html
            });
        } finally {
            setIsLoading(false);
        }
    };

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
                            placeholder="아이디를 입력하세요"
                            value={loginId}
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
                        <input
                            type="text"
                            className="form-control form-control-lg"
                            placeholder="닉네임을 입력하세요"
                            value={nickname}
                            onChange={(event) => setNickName(event.target.value)}
                            onKeyDown={handleEnterKey}
                        />
                        <input
                            type="text"
                            className="form-control form-control-lg"
                            placeholder="이름을 입력하세요"
                            value={name}
                            onChange={(event) => setName(event.target.value)}
                            onKeyDown={handleEnterKey}
                        />
                    </div>
                    <div className="kw-login-button">
                        <button type="button" className="btn btn-primary btn-lg" onClick={handleClickLogin}>회원가입</button>
                    </div>
                    {/*<div className="kw-login-text d-flex justify-content-between">*/}
                    {/*    <Link to={"#"}><img src={kakaoLogin} style={{ width: "200px" }}/></Link>*/}
                    {/*    <Link to={"#"}><img src={googleLogin} style={{ width: "200px" }}/></Link>*/}
                    {/*</div>*/}
                    <div className="kw-login-text">
                        <Stack direction={"horizontal"}>
                            <p>이미 아이디가 있다면?</p>
                            <Link className={"ms-auto"} to={"/login"}>로그인</Link>
                        </Stack>
                    </div>
                </div>
            </div>
            <Spinner show={isLoading}/>
        </>
    );
}

export default Signup;