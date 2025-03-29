package sns.pinocchio.domain.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.auth.AuthService;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.SignupRequestDto;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class AuthServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void init() {
        Member member = Member.builder()
                .email("example@naver.com")
                .name("name")
                .nickname("nickname")
                .password(passwordEncoder.encode("memberPassword123!!@"))
                .build();

        memberRepository.save(member);
    }

    @Test
    @DisplayName("계정 생성 테스트")
    public void createMemberTest() {
        SignupRequestDto member = new SignupRequestDto("name", "example1@naver.com", "nickname1", "memberPassword123!@");
        memberService.createMember(member);

        Member user = memberService.findByEmail("example1@naver.com");

        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getNickname()).isEqualTo("nickname1");
    }

    @Test
    @DisplayName("이메일 검증 성공 테스트")
    public void validateEmail() {
        assertThatCode(() -> memberService.findByEmail("example@naver.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 검증 실패 테스트")
    public void validateEmail_Failure() {
        assertThatThrownBy(() -> memberService.findByEmail("notfound@naver.com"))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다.");
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

    @AfterEach
    public void cleanup() {
        // 테스트가 끝난 후 데이터베이스 초기화
        memberRepository.deleteAll(); // 모든 데이터를 삭제
    }
}