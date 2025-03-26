package sns.pinocchio.config.global.auth.jwt;

import com.fasterxml.jackson.core.ObjectCodec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.exception.AuthErrorCode;
import sns.pinocchio.config.global.auth.exception.AuthException;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.config.global.auth.service.CookieService;
import sns.pinocchio.config.global.auth.service.CustomUserDetailService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.persistence.redis.redisService.RedisService;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MemberAuthFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final CookieService cookieService;
    private final RedisService redisService;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;
    private final TokenProvider tokenProvider;

    ObjectCodec objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String accessToken = cookieService.getAccessTokenFromCookie(request);
        final String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        // 엑세스 토큰과 리프레쉬 토큰이 null일 경우 에러 응답
        // 엑세스 토큰만 null일 경우 리프레쉬 토큰으로 토큰 재발급 후 인증
        if (accessToken == null) {
            if (refreshToken == null) {
                handleAuthError(AuthErrorCode.AUTHORIZATION_FAILED, request, response);
                return;
            }
            String reissuedAccessToken = reissueToken(refreshToken, request, response);
            setAuthenticationInContext(reissuedAccessToken);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            TokenStatus tokenStatus = jwtUtil.validateToken(accessToken);

            switch (tokenStatus) {
                case VALID:
                    setAuthenticationInContext(accessToken);
                    break;

                case EXPIRED:
                    log.info("만료된 토큰입니다.");
                    String reissuedAccessToken = reissueToken(refreshToken, request, response);
                    setAuthenticationInContext(reissuedAccessToken);
                    break;

                case MALFORMED, INVALID:
                    log.error("잘못된 형식의 토큰입니다.");
                    handleAuthError(AuthErrorCode.INVALID_TOKEN, request, response);
                    return;
            }
        } catch (Exception e) {
            log.error("필터 내부에서 예상치 못한 예외 발생: {}", e.getMessage());
            handleAuthError(AuthErrorCode.AUTHORIZATION_FAILED, request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    // 사용자 권한이 필요한 api 경로만 필터링을 하는 메서드
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        String method = request.getMethod();

        return (
                (method.equals("GET") && path.equals("/api/posts/search")) ||
                (method.equals("POST") && path.equals("/api/auth/signup")) ||
                (method.equals("POST") && path.equals("/api/auth/login")) ||
                (method.equals("POST") && path.equals("/api/auth/logout"))
        );
    }

    // SecurityContext에 인증 정보를 주입하는 메서드
    private void setAuthenticationInContext(String accessToken) {
        CustomUserDetails customUserDetails = customUserDetailService.loadUserByAccessToken(accessToken);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }


    // 인증에 실패했을 때 로그아웃 처리 및 에러 응답 메서드
    private void handleAuthError(AuthErrorCode ex,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException {

        cookieService.clearTokenFromCookie(response);
        SecurityContextHolder.clearContext();

        throw new AuthException(ex);
    }


    // 토큰 재발급 및 쿠키에 토큰 정보 저장하는 메서드
    private String reissueToken(String refreshToken, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(!redisService.exists(refreshToken)) {
            handleAuthError(AuthErrorCode.INVALID_TOKEN, request, response);
        }
        String memberId = redisService.get(refreshToken);
        Member member = memberService.findByUserId(Long.valueOf(memberId));
        String newAccessToken = tokenProvider.generateAccessToken(member);
        cookieService.addAccessTokenToCookie(newAccessToken, response);
        return refreshToken;
    }
}