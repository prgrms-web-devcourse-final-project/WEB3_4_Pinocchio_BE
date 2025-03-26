package sns.pinocchio.application.member;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.member.memberDto.MemberRequestDto;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 유저 생성
    @Transactional
    public void createMember(MemberRequestDto memberRequestDto) {

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new MemberException(MemberErrorCode.EMAIL_DUPLICATED);
        }

        Member member = Member.builder()
                .email(memberRequestDto.getEmail())
                .name(memberRequestDto.getName())
                .nickname(memberRequestDto.getNickname())
                .password(passwordEncoder.encode(memberRequestDto.getPassword()))
                .build();

        this.memberRepository.save(member);
    }
}
