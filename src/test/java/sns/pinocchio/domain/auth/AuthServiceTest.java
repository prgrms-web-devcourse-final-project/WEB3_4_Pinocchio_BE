package sns.pinocchio.domain.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.PinocchioApplication;
import sns.pinocchio.application.auth.AuthService;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Tag("unit")
@SpringBootTest(classes = PinocchioApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AuthServiceTest {

  @Autowired private MemberService memberService;

  @Autowired private MemberRepository memberRepository;

  @Autowired private AuthService authService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private TokenProvider tokenProvider;

  @BeforeEach
  public void init() {
    Member member =
        Member.builder()
            .email("example@naver.com")
            .name("name")
            .nickname("nickname")
            .password(passwordEncoder.encode("memberPassword123!!@"))
            .build();

    memberRepository.save(member);
  }

  @Test
  @DisplayName("패스워드 검증 성공 테스트")
  public void validatePassword_Success() {
    Member member = memberService.findByEmail("example@naver.com");

    assertThatCode(() -> authService.validatePassword("memberPassword123!!@", member))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("패스워드 검증 실패 테스트")
  public void validatePassword_Failure() {
    Member member = memberService.findByEmail("example@naver.com");

    assertThatThrownBy(() -> authService.validatePassword("notFoundPassword123!", member))
        .isInstanceOf(MemberException.class)
        .hasMessageContaining("비밀번호가 올바르지 않습니다.");
  }

  @Test
  @DisplayName("토큰 생성 테스트")
  public void createToken() {
    Member member = memberService.findByEmail("example@naver.com");

    String accessToken = tokenProvider.generateAccessToken(member);
    String refreshToken = tokenProvider.generateRefreshToken();

    assertThat(accessToken).isNotNull();
    assertThat(refreshToken).isNotNull();
  }
}
