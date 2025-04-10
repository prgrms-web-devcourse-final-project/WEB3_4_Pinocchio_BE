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
        // 이메일 공백 + 형식 체크
        if (isEmptyOrNull(loginId)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '이메일은 필수 항목입니다.'
            });
            return false;
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(loginId)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '올바른 이메일 형식이 아닙니다.'
            });
            return false;
        }

        // 비밀번호 공백 + 길이 + 특수문자 포함
        if (isEmptyOrNull(password)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '비밀번호는 필수 항목입니다.'
            });
            return false;
        }
        if (password.length < 8) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '비밀번호는 8자 이상이어야 합니다.'
            });
            return false;
        }
        const passwordRegex = /^(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
        if (!passwordRegex.test(password)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '비밀번호는 특수문자를 포함해야 합니다.'
            });
            return false;
        }

        // 닉네임 공백 + 길이 체크
        if (isEmptyOrNull(nickname)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '닉네임은 필수 항목입니다.'
            });
            return false;
        }
        if (nickname.length < 3 || nickname.length > 20) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '닉네임은 3자 이상 20자 이하로 입력해주세요.'
            });
            return false;
        }

        // 이름 공백 체크
        if (isEmptyOrNull(name)) {
            openConfirm({
                title: '회원가입 중 오류가 발생했습니다.',
                html: '이름은 필수 항목입니다.'
            });
            return false;
        }

        // 모든 검증 통과 시 백엔드로 전송
        const sendData = {
            email: loginId,
            password,
            nickname,
            name
        };
        requestSignup(sendData);
    };

    const requestSignup = async (sendData) => {
        try {
            setIsLoading(true);
            const response = await axios.post('/auth/signup', sendData);
            // localStorage.setItem("token", response.data);
            navigate('/login')
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