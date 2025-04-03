package sns.pinocchio.domain.block;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import sns.pinocchio.application.blockedUser.BlockedUserService;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.blockedUser.BlockedUserRepository;
import sns.pinocchio.infrastructure.member.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BlockControllerTest {

  @Autowired private BlockedUserService blockedUserService;

  @Autowired private BlockedUserRepository blockedUserRepository;

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
  @DisplayName("유저 차단 테스트")
  public void blockUserTest() throws Exception {
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    ResultActions blockUserResponse =
        mockMvc.perform(
            post("/block/1")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON));

    Member member = memberService.findByEmail("example@naver.com");
    boolean result =
        blockedUserRepository.existsByBlockerUserIdAndBlockedUserId(member.getId(), 1L);

    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("유저 차단 해제 테스트")
  public void unblockUserTest() throws Exception {
    Member member = memberService.findByEmail("example@naver.com");
    blockedUserService.saveBlock(member.getId(), 1L);

    boolean trueResult =
        blockedUserRepository.existsByBlockerUserIdAndBlockedUserId(member.getId(), 1L);
    assertThat(trueResult).isTrue();

    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    mockMvc.perform(
        delete("/block/1")
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON));

    boolean falseResult =
        blockedUserRepository.existsByBlockerUserIdAndBlockedUserId(member.getId(), 1L);
    assertThat(falseResult).isFalse();
  }

  @Test
  @DisplayName("유저 차단 조회 테스트")
  public void getBlockUsersTest() throws Exception {
    Member member = memberService.findByEmail("example@naver.com");
    blockedUserService.saveBlock(member.getId(), 1L);

    boolean trueResult =
        blockedUserRepository.existsByBlockerUserIdAndBlockedUserId(member.getId(), 1L);
    assertThat(trueResult).isTrue();

    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

    ResultActions getBlockMemberResponse =
        mockMvc.perform(
            get("/block")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON));

    getBlockMemberResponse.andExpect(status().isOk());
  }
}
