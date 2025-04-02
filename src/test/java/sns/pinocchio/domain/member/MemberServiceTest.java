package sns.pinocchio.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.PinocchioApplication;
import sns.pinocchio.application.auth.AuthService;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;
import sns.pinocchio.application.member.memberDto.request.UpdateRequestDto;
import sns.pinocchio.application.report.ReportService;
import sns.pinocchio.domain.report.Report;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static sns.pinocchio.domain.report.ReportedType.POST;

@SpringBootTest(classes = PinocchioApplication.class)
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class MemberServiceTest {

  @Autowired private MemberService memberService;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ReportService reportService;
  @Autowired private AuthService authService;

  @BeforeEach
  public void init() {
    memberRepository.deleteAll();
    Member member =
        Member.builder()
            .email("example@naver.com")
            .password("memberPassword123!")
            .nickname("nickname")
            .name("member")
            .build();

    memberRepository.save(member);
    List<Member> members = memberRepository.findAll();
    members.forEach(System.out::println);
  }

  @Test
  @DisplayName("계정 생성 테스트")
  public void createMemberTest() {
    SignupRequestDto member =
        new SignupRequestDto("name", "exampletest@naver.com", "testNickname", "memberPassword123@");
    memberService.createMember(member);

    Member user = memberService.findByEmail("exampletest@naver.com");

    assertThat(user).isNotNull();
    assertThat(user.getName()).isEqualTo("name");
    assertThat(user.getNickname()).isEqualTo("testNickname");
    assertThat(user.getTsid()).isNotNull();
  }

  @Test
  @DisplayName("이메일 검증 성공 테스트")
  public void validateEmail() {
    assertThatCode(() -> memberService.findByEmail("example@naver.com")).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 검증 실패 테스트")
  public void validateEmail_Failure() {
    assertThatThrownBy(() -> memberService.findByEmail("notfound@naver.com"))
        .isInstanceOf(MemberException.class)
        .hasMessageContaining("존재하지 않는 사용자입니다.");
  }

  @Test
  @DisplayName("유저 프로필 수정 테스트")
  public void updateProfileTest() {
    UpdateRequestDto updateRequestDto =
        new UpdateRequestDto("nana", "Nick", "안녕하세요", "youtube", "", false);
    Member member = memberService.findByEmail("example@naver.com");
    member.updateProfile(updateRequestDto);

    Member result = memberService.findByEmail("example@naver.com");

    assertThat(result.getName()).isEqualTo("nana");
    assertThat(result.getNickname()).isEqualTo("Nick");
    assertThat(result.getBio()).isEqualTo("안녕하세요");
    assertThat(result.getWebsite()).isEqualTo("youtube");
    assertThat(result.getProfileImageUrl()).isEmpty();
    assertThat(result.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("패스워드 변경 테스트")
  public void modifyPasswordTest() {
    Member member = memberService.findByEmail("example@naver.com");

    memberService.changePassword(member, "abcd123@");

    assertThatCode(() -> authService.validatePassword("abcd123@", member))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("신고 내역 저장 테스트")
  public void createReportTest() {
    reportService.createReport(1L, 2L, POST, "욕설");
    Report report = reportService.findByReporter(1L);

    assertThat(report.getReportedId()).isEqualTo(2L);
    assertThat(report.getReportedType()).isEqualTo(POST);
    assertThat(report.getReason()).isEqualTo("욕설");
  }
}
