package sns.pinocchio.application.member;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.member.memberDto.SignupRequestDto;
import sns.pinocchio.application.member.memberDto.UpdateRequestDto;
import sns.pinocchio.config.global.auth.util.EmailUtil;
import sns.pinocchio.config.global.auth.util.PasswordUtil;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 계정 생성
    @Transactional
    public Member createMember(SignupRequestDto signupRequestDto) {
        // 이메일 중복 체크
        checkEmailDuplicate(signupRequestDto.email());

        // 닉네임 중복 체크
        checkNicknameDuplicate(signupRequestDto.nickname());

        Member member = Member.builder()
                .email(signupRequestDto.email())
                .name(signupRequestDto.name())
                .nickname(signupRequestDto.nickname())
                .password(passwordEncoder.encode(signupRequestDto.password()))
                .build();

        this.memberRepository.save(member);

        return member;
    }

    // 사용자 프로필 수정
    @Transactional
    public Member updateProfile(Long memberId, UpdateRequestDto updateRequestDto) {
        // 유저 확인
        Member member = findById(memberId);

        // 닉네임 중복 체크
        checkNicknameDuplicate(updateRequestDto.nickname());

        member.updateProfile(updateRequestDto);
        return member;
    }

    // 임시 비밀번호 발송
    @Transactional
    public void sendTemporaryPassword(String email) {
        // 회원 조회
        Member member = findByEmail(email);

        // 임시 비밀번호 생성 / 패스워드 암호화 / 비밀번호 변경
        String temporaryPassword = PasswordUtil.generateTemporaryPassword();
        member.updatePassword(passwordEncoder.encode(temporaryPassword));

        // 이메일 발송
        EmailUtil.sendEmail(member.getEmail(),temporaryPassword);
    }

    // 패스워드 수정
    @Transactional
    public void changePassword(Member member, String password) {
        member.updatePassword(passwordEncoder.encode(password));
    }

    // 사용자 email 조회
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
    }

    // 사용자 nickname 조회
    public Member findByNickname(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NICKNAME_DUPLICATED));
    }

    // 사용자 ID 조회
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
    }

    // 사용자 email 중복 확인
    public void checkEmailDuplicate(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(member -> {
                    throw new MemberException(MemberErrorCode.EMAIL_DUPLICATED);
                });
    }

    // 사용자 nickname 중복 확인
    public void checkNicknameDuplicate(String nickname) {
        memberRepository.findByNickname(nickname)
                .ifPresent(member -> {
                    throw new MemberException(MemberErrorCode.NICKNAME_DUPLICATED);
                });
    }

    // 사용자 삭제
    public void deleteMember(Member member) {
        memberRepository.deleteById(member.getId());
    }

    public Member findByTsid(String tsid) {
        return memberRepository.findByTsid(tsid)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
    }
}