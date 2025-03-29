package sns.pinocchio.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.UpdateRequestDto;
import sns.pinocchio.infrastructure.member.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    public void init() {
        Member member = Member.builder()
                .email("example1@naver.com")
                .password("memberPassword123!")
                .nickname("nickname1")
                .name("member")
                .build();

        memberRepository.save(member);
    }

    @Test
    @DisplayName("유저 조회 테스트")
    public void getMemberTest() {
        Member member = memberService.findByEmail("example1@naver.com");

        assertThat(member.getName()).isEqualTo("member");
        assertThat(member.getEmail()).isEqualTo("example1@naver.com");
        assertThat(member.getNickname()).isEqualTo("nickname1");
        assertThat(member.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("유저 프로필 수정 테스트")
    public void updateProfileTest() {
        UpdateRequestDto updateRequestDto = new UpdateRequestDto("nana", "Nick", "안녕하세요", "youtube", "", false);
        Member member = memberService.findByEmail("example1@naver.com");
        member.updateProfile(updateRequestDto);

        Member result = memberService.findByEmail("example1@naver.com");

        assertThat(result.getName()).isEqualTo("nana");
        assertThat(result.getNickname()).isEqualTo("Nick");
        assertThat(result.getBio()).isEqualTo("안녕하세요");
        assertThat(result.getWebsite()).isEqualTo("youtube");
        assertThat(result.getProfileImageUrl()).isEmpty();
        assertThat(result.getIsActive()).isFalse();
    }

}