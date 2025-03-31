package sns.pinocchio.config.global.auth.jwt;

import com.fasterxml.jackson.core.ObjectCodec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.config.global.auth.service.CustomUserDetailService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.presentation.auth.exception.AuthErrorCode;
import sns.pinocchio.presentation.auth.exception.AuthException;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MemberAuthFilter extends OncePerRequestFilter {

    private final MemberService memberService;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;

    ObjectCodec objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String accessToken = getTokenFromHeader(request, HttpHeaders.AUTHORIZATION);


        try {
            TokenStatus tokenStatus = jwtUtil.validateToken(accessToken);

            switch (tokenStatus) {
                case VALID:
                    setAuthenticationInContext(accessToken);
                    break;

                case EXPIRED:
                    log.info("만료된 토큰입니다.");
                    handleAuthError(AuthErrorCode.TOKEN_EXPIRED);
                    break;

                case MALFORMED, INVALID:
                    log.error("잘못된 형식의 토큰입니다.");
                    handleAuthError(AuthErrorCode.INVALID_TOKEN);
                    return;
            }
        } catch (Exception e) {
            log.error("필터 내부에서 예상치 못한 예외 발생: {}", e.getMessage());
            handleAuthError(AuthErrorCode.AUTHORIZATION_FAILED);
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
                        (method.equals("POST") && path.equals("/api/auth/logout")) ||
                        path.startsWith("/swagger") ||
                        path.startsWith("/v3/api-docs") ||
                        path.startsWith("/swagger-ui") ||
                        path.startsWith("/swagger-resources") ||
                        path.startsWith("/webjars")
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
    private void handleAuthError(AuthErrorCode ex) {
        SecurityContextHolder.clearContext();
        throw new AuthException(ex);
    }

    // 헤더에서 토큰 가져오는 메서드
    private String getTokenFromHeader(HttpServletRequest request, String headerName) {
        String token = request.getHeader(headerName);
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}