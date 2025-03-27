package sns.pinocchio.domain.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.MemberRequestDto;
import sns.pinocchio.infrastructure.member.MemberRepository;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    public ResultActions loginAndGetResponse() throws Exception {
        String loginRequestJson = """
            {
                "email" : "example@naver.com",
                "password": "memberPassword123!"
            }
        """.trim();

        return mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestJson)
        );
    }

    @BeforeEach
    public void init() {
        memberRepository.deleteAll();
        MemberRequestDto member = new MemberRequestDto("member", "example@naver.com", "nick", "memberPassword123!");
        memberService.createMember(member);
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void signupTest() throws Exception {
        String signupRequestJson = """
            {
                "name" : "member",
                "email" : "example1@naver.com",
                "nickname" : "nickname",
                "password": "memberPassword123!"
            }
        """.trim();

        ResultActions signupResponse =  mockMvc.perform(
                post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestJson)
        );

        boolean result = memberRepository.existsByEmail("example1@naver.com");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("로그인 테스트")
    public void loginSuccessTest() throws Exception {
        ResultActions loginResponse = loginAndGetResponse();

        // 응답 상태와 쿠키 검증
        loginResponse
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().secure("refreshToken", true));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    public void logoutSuccessTest() throws Exception {
        // 로그인 후 응답 받기
        ResultActions loginResponse = loginAndGetResponse();

        // 로그인 후 쿠키가 존재하고, HttpOnly, Secure 설정이 잘 되어있는지 확인
        String accessToken = loginResponse.andReturn().getResponse().getCookie("accessToken").getValue();
        String refreshToken = loginResponse.andReturn().getResponse().getCookie("refreshToken").getValue();

        // 쿠키가 null이 아니어야 함을 검증
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // 로그아웃 요청
        ResultActions logoutResponse = mockMvc.perform(post("/api/auth/logout") // 로그아웃 경로를 확인해주세요.
                .cookie(new Cookie("accessToken", accessToken))
                .cookie(new Cookie("refreshToken", refreshToken)));

        logoutResponse
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().value("refreshToken", ""));
    }
}