package sns.pinocchio.application.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.member.memberDto.MemberInfoDto;
import sns.pinocchio.application.member.memberDto.MemberRequestDto;
import sns.pinocchio.config.global.auth.service.CookieService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.infrastructure.persistence.redis.redisService.RedisService;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    private final RedisService redisService;
    private final TokenProvider tokenProvider;
    private final JwtUtil jwtUtil;

    // 계정 생성
    @Transactional
    public void createMember(MemberRequestDto memberRequestDto) {

        // 이메일 중복 체크
        if (memberRepository.existsByEmail(memberRequestDto.email())) {
            throw new MemberException(MemberErrorCode.EMAIL_DUPLICATED);
        }

        Member member = Member.builder()
                .email(memberRequestDto.email())
                .name(memberRequestDto.name())
                .nickname(memberRequestDto.nickname())
                .password(passwordEncoder.encode(memberRequestDto.password()))
                .build();

        this.memberRepository.save(member);
    }

    // 이메일 검증
    public void validateEmail(String email) {
        if(memberRepository.existsByEmail(email)) {
            return;
        }
        throw new MemberException(MemberErrorCode.EMAIL_NOT_FOUND);
    }

    // 패스워드 검증
    public void validatePassword(String password, String email) {
        Member member = findByEmail(email);

        if(passwordEncoder.matches(password, member.getPassword())) {
            return;
        }
        throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
    }

    // 엑세스토큰, 리프래시토큰 생성 및 저장
    public void generateAndSaveToken(String email, HttpServletResponse response) {
        Member member = findByEmail(email);

        String accessToken = tokenProvider.generateAccessToken(member);
        String refreshToken = tokenProvider.generateRefreshToken();

        cookieService.addAccessTokenToCookie(accessToken, response);
        cookieService.addRefreshTokenToCookie(refreshToken, response);
        redisService.save(refreshToken, String.valueOf(member.getId()), jwtUtil.getRefreshTokenExpirationTime());
    }

    // 토큰 제거
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieService.getAccessTokenFromCookie(request);
        MemberInfoDto member = jwtUtil.getMemberInfoDto(accessToken);

        redisService.addBlackList(String.valueOf(member.id()), jwtUtil.getRefreshTokenExpirationTime());
        cookieService.clearTokenFromCookie(response);
    }

    public Member findByUserId(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
    }

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.USER_NOT_FOUND));
    }
}
