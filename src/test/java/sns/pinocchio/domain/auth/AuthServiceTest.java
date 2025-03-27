package sns.pinocchio.domain.auth;

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
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.MemberRequestDto;
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void init() {
        MemberRequestDto member = new MemberRequestDto("member", "example@naver.com", "nick", "memberPassword123!");
        memberService.createMember(member);
    }

    @Test
    @DisplayName("계정 생성 테스트")
    public void createMemberTest() {
        boolean result = memberRepository.existsByEmail("example@naver.com");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이메일 검증 성공 테스트")
    public void validateEmail() {
        assertThatCode(() -> memberService.validateEmail("example@naver.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 검증 실패 테스트")
    public void validateEmail_Failure() {
        assertThatThrownBy(() -> memberService.validateEmail("notfound@naver.com"))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("이메일이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("패스워드 검증 성공 테스트")
    public void validatePassword_Success() {
        assertThatCode(() -> memberService.validatePassword("memberPassword123!", "example@naver.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("패스워드 검증 실패 테스트")
    public void validatePassword_Failure() {
        assertThatThrownBy(() -> memberService.validatePassword("notFoundPassword123!", "example@naver.com"))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining("비밀번호가 올바르지 않습니다.");
    }
}