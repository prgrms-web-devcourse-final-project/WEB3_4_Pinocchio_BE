package sns.pinocchio.application.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.member.memberDto.MemberInfoDto;
import sns.pinocchio.config.global.auth.service.CookieService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.persistence.redis.redisService.RedisService;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    private final RedisService redisService;
    private final TokenProvider tokenProvider;
    private final JwtUtil jwtUtil;

    // 패스워드 검증
    public void validatePassword(String password, Member member) {
        if(!passwordEncoder.matches(password, member.getPassword())) {
            throw new MemberException(MemberErrorCode.INVALID_PASSWORD);
        }
    }

    // 엑세스토큰, 리프래시토큰 생성 및 저장
    public void generateAndSaveToken(Member member, HttpServletResponse response) {

        String accessToken = tokenProvider.generateAccessToken(member);
        String refreshToken = tokenProvider.generateRefreshToken();

        cookieService.addAccessTokenToCookie(accessToken, response);
        cookieService.addRefreshTokenToCookie(refreshToken, response);
        redisService.save("refresh_token: " + refreshToken, "userId: " + member.getId(), jwtUtil.getRefreshTokenExpirationTime());
    }

    // 토큰 제거
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieService.getAccessTokenFromCookie(request);
        MemberInfoDto member = jwtUtil.getMemberInfoDto(accessToken);

        redisService.addBlackList(String.valueOf(member.id()), jwtUtil.getRefreshTokenExpirationTime());
        cookieService.clearTokenFromCookie(response);
    }
}