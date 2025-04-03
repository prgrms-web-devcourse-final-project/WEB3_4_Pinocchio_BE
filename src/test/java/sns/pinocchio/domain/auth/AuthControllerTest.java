package sns.pinocchio.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MemberService memberService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private JwtUtil jwtUtil;

  private ResultActions loginAndGetResponse() throws Exception {
    String loginRequestJson =
        TestFixture.createLoginRequestJson("example@naver.com", "testPassword!");

    return mockMvc.perform(
        post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequestJson));
  }

  @BeforeEach
  public void setUp() {
    Member member =
        Member.builder()
            .email("example@naver.com")
            .password(passwordEncoder.encode("testPassword!"))
            .name("testName")
            .nickname("testNickname")
            .build();
    memberRepository.save(member);
  }

  @Test
  @DisplayName("회원가입 성공 테스트")
  public void testSignUpSuccess() throws Exception {
    String signUpRequestJson =
        TestFixture.createSignUpRequestJson("bob", "bob123@gmail.com", "bobBurger", "hamBurger!");

    ResultActions signUpResponse =
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signUpRequestJson));

    signUpResponse.andExpect(status().isCreated());

    Member member = memberService.findByEmail("bob123@gmail.com");
    assertThat(member.getName()).isEqualTo("bob");
    assertThat(member.getNickname()).isEqualTo("bobBurger");
    assertThat(member.getTsid()).isNotNull();
    assertThat(passwordEncoder.matches("hamBurger!", member.getPassword())).isTrue();
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 이메일 중복")
  public void testSignUpFail_DuplicateEmail() throws Exception {
    String signUpRequestJson =
        TestFixture.createSignUpRequestJson("bob", "example@naver.com", "bobBurger", "hamBurger!");

    ResultActions signUpResponse =
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signUpRequestJson));

    signUpResponse
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."))
        .andExpect(jsonPath("$.code").value("USER_409"));
  }

  @Test
  @DisplayName("회원가입 실패 테스트 - 닉네임 중복")
  public void testSignUpFail_DuplicateNickname() throws Exception {
    String signUpRequestJson =
        TestFixture.createSignUpRequestJson(
            "bob", "bob123@gmail.com", "testNickname", "hamBurger!");

    ResultActions signUpResponse =
        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(signUpRequestJson));

    signUpResponse
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").value("이미 사용 중인 닉네임입니다."))
        .andExpect(jsonPath("$.code").value("USER_409"));
  }

  @Test
  @DisplayName("로그인 성공 테스트")
  public void testLoginSuccess() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    loginResponse
        .andExpect(status().isOk())
        .andExpect(header().exists("Authorization"))
        .andExpect(header().string("Authorization", accessToken))
        .andExpect(cookie().exists("refreshToken"))
        .andExpect(cookie().httpOnly("refreshToken", true))
        .andExpect(cookie().secure("refreshToken", true));
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 이메일 불일치")
  public void testLoginFail_InvalidEmail() throws Exception {
    String loginRequestJson =
        TestFixture.createLoginRequestJson("example1@naver.com", "testPassword!");
    ResultActions loginResponse =
        mockMvc.perform(
            post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequestJson));

    loginResponse
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.statusCode").value(404))
        .andExpect(jsonPath("$.message").value("존재하지 않는 사용자입니다."))
        .andExpect(jsonPath("$.code").value("USER_404"));
  }

  @Test
  @DisplayName("로그인 실패 테스트 - 패스워드 불일치")
  public void testLoginFail_InvalidPassword() throws Exception {
    String loginRequestJson = TestFixture.createLoginRequestJson("example@naver.com", "Password!");
    ResultActions loginResponse =
        mockMvc.perform(
            post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequestJson));

    loginResponse
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.statusCode").value(401))
        .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."))
        .andExpect(jsonPath("$.code").value("USER_401"));
  }

  @Test
  @DisplayName("로그아웃 성공 테스트")
  public void testLogoutSuccess() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    Cookie refreshTokenCookie = loginResponse.andReturn().getResponse().getCookie("refreshToken");
    String refreshTokenValue = refreshTokenCookie != null ? refreshTokenCookie.getValue() : null;

    assertThat(refreshTokenValue).isNotNull();

    ResultActions logoutResponse =
        mockMvc.perform(
            post("/auth/logout")
                .cookie(new Cookie("refreshToken", refreshTokenValue))
                .contentType(MediaType.APPLICATION_JSON));

    logoutResponse
        .andExpect(status().isOk()) // 로그아웃 성공 상태 코드 확인
        .andExpect(cookie().value("refreshToken", ""));
  }

  @Test
  @DisplayName("엑세스토큰 재발급 테스트")
  public void test123() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    Cookie refreshTokenCookie = loginResponse.andReturn().getResponse().getCookie("refreshToken");
    String refreshTokenValue = refreshTokenCookie.getValue();

    Member member = memberService.findByEmail("example@naver.com");

    String expiredAccessToken =
        Jwts.builder()
            .setSubject(member.getName()) // 사용자 이름
            .claim("id", member.getId()) // 사용자 ID
            .claim("email", member.getEmail()) // 사용자 이메일
            .claim("tsid", member.getTsid())
            .setIssuedAt(new Date()) // 발급 시간 (현재 시간)
            .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 만료 시간을 과거로 설정
            .signWith(jwtUtil.getKey()) // 시크릿 키
            .compact();

    ResultActions memberSearchResponse =
        mockMvc.perform(
            post("/user")
                .header("Authorization", "Bearer " + expiredAccessToken)
                .cookie(new Cookie("refreshToken", refreshTokenValue))
                .contentType(MediaType.APPLICATION_JSON));

    String newAccessToken =
        memberSearchResponse.andReturn().getResponse().getHeader("Authorization");

    assertThat(newAccessToken).isNotNull();
  }
}
