package sns.pinocchio.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static sns.pinocchio.domain.report.ReportedType.POST;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
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
import sns.pinocchio.application.report.ReportService;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.report.Report;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberException;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MemberControllerTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private MemberRepository memberRepository;

  @Autowired private MemberService memberService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private ReportService reportService;

  @PersistenceContext private EntityManager em;

  private ResultActions loginAndGetResponse() throws Exception {
    String loginRequestJson =
        TestFixture.createLoginRequestJson("example@naver.com", "testPassword!");

    return mockMvc.perform(
        post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequestJson));
  }

  private String getRefreshToken(ResultActions loginResponse) {
    return loginResponse.andReturn().getResponse().getCookie("refreshToken").getValue();
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
  @DisplayName("JWT 토큰으로 프로필 변경 테스트")
  public void testUpdateProfileSuccess() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    String updateRequestDto =
        TestFixture.updateProfileRequestJson("abc", "lol", "hello", "abc@youtube", "null", false);

    ResultActions getProfileResponse =
        mockMvc.perform(
            put("/member")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestDto));

    getProfileResponse.andExpect(status().isOk());

    Member member = memberService.findByEmail("example@naver.com");

    assertThat(member.getName()).isEqualTo("abc");
    assertThat(member.getNickname()).isEqualTo("lol");
    assertThat(member.getBio()).isEqualTo("hello");
    assertThat(member.getWebsite()).isEqualTo("abc@youtube");
    assertThat(member.getProfileImageUrl()).isEqualTo("null");
    assertThat(member.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("비밀번호 변경 테스트")
  public void testChangePasswordSuccess() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    String changePasswordDto = TestFixture.createNewPassword("testPassword!", "spongebob123!");

    ResultActions changePasswordResponse =
        mockMvc.perform(
            put("/member/password")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(changePasswordDto));

    changePasswordResponse.andExpect(status().isOk());

    Member member = memberService.findByEmail("example@naver.com");
    assertThat(passwordEncoder.matches("spongebob123!", member.getPassword())).isTrue();
  }

  @Test
  @DisplayName("회원탈퇴 테스트")
  public void testDeleteMemberSuccess() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
    String refreshTokenValue = getRefreshToken(loginResponse);

    String passwordDto = TestFixture.createPassword("testPassword!");

    ResultActions deleteMemberResponse =
        mockMvc.perform(
            delete("/member")
                .header("Authorization", accessToken)
                .cookie(new Cookie("refreshToken", refreshTokenValue))
                .contentType(MediaType.APPLICATION_JSON)
                .content(passwordDto));

    deleteMemberResponse.andExpect(status().isOk());

    assertThatThrownBy(() -> memberService.findByEmail("example@naver.com"))
        .isInstanceOf(MemberException.class)
        .hasMessageContaining("존재하지 않는 사용자입니다.");
  }

  @Test
  @DisplayName("신고 저장 테스트")
  public void testReport() throws Exception {
    Member member =
        Member.builder()
            .email("exampletest2@naver.com")
            .password(passwordEncoder.encode("testPassword!"))
            .name("test1")
            .nickname("test1")
            .build();
    memberRepository.save(member);

    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    String reportRequestDto = TestFixture.createReportRequestDto("test1", POST, "욕설 및 무단침입 강도 도둑");

    ResultActions reportResponse =
        mockMvc.perform(
            post("/member/report")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(reportRequestDto));

    reportResponse
        .andExpect(status().isOk())
        .andExpect(content().string("계정 신고가 완료되었습니다. 신고 내용은 검토 후 처리됩니다."));
    Long memberId = memberService.findByEmail("example@naver.com").getId();
    Report report = reportService.findByReporter(memberId);

    assertThat(report.getReportedType()).isEqualTo(POST);
    assertThat(report.getReason()).isEqualTo("욕설 및 무단침입 강도 도둑");
  }
}
