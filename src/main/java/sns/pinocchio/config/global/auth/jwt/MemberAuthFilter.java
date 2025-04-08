package sns.pinocchio.config.global.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.config.global.auth.service.CustomUserDetailService;
import sns.pinocchio.config.global.auth.service.cookieService.CookieService;
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.config.global.auth.util.TokenProvider;
import sns.pinocchio.infrastructure.redis.redisService.RedisService;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.presentation.auth.exception.AuthErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberAuthFilter extends OncePerRequestFilter {

  private final CustomUserDetailService customUserDetailService;
  private final CookieService cookieService;
  private final TokenProvider tokenProvider;
  private final RedisService redisService;
  private final MemberService memberService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String accessToken = getTokenFromHeader(request, HttpHeaders.AUTHORIZATION);
    final String refreshToken = cookieService.getRefreshTokenFromCookie(request);

    if (accessToken == null) {
      log.debug("인증 토큰이 없습니다.");
      handleAuthError(response, AuthErrorCode.INVALID_TOKEN);
    }

    try {
      TokenStatus tokenStatus = JwtUtil.validateToken(accessToken);

      switch (tokenStatus) {
        case VALID:
          setAuthenticationInContext(accessToken);
          break;

        case EXPIRED:
          log.info("만료된 토큰입니다.");
          String newAccessToken = reissueToken(refreshToken, request, response);
          setAuthenticationInContext(newAccessToken);
          break;

        case MALFORMED, INVALID:
          log.error("잘못된 형식의 토큰입니다.");
          handleAuthError(response, AuthErrorCode.INVALID_TOKEN);
          return;
      }
    } catch (Exception e) {
      log.error("필터 내부에서 예상치 못한 예외 발생: {}", e.getMessage());
      handleAuthError(response, AuthErrorCode.AUTHORIZATION_FAILED);
      return;
    }
    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

    String path = request.getRequestURI();
    String method = request.getMethod();
    log.info("요청 경로: {}, 메서드: {}", path, method);

    boolean shouldSkip =
        (method.equals("GET")
                && (path.equals("/api/posts/search")
                    || path.startsWith("/actuator/health")
                    || path.startsWith("/api/actuator/health")))
            || (method.equals("POST") && (path.startsWith("/auth") || path.startsWith("/api/auth")))
            || (method.equals("POST")
                && (path.startsWith("/user/password/reset")
                    || path.startsWith("/api/user/password/reset")))
            || path.startsWith("/swagger")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/swagger-resources")
            || path.startsWith("/webjars");

    log.info("필터 건너뛰기: {}", shouldSkip);
    return shouldSkip;
  }

  private void setAuthenticationInContext(String accessToken) {
    CustomUserDetails customUserDetails =
        customUserDetailService.loadUserByAccessToken(accessToken);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
  }

  private void handleAuthError(HttpServletResponse response, AuthErrorCode errorCode)
      throws IOException {
    SecurityContextHolder.clearContext();

    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", errorCode.getHttpStatus().value());
    errorResponse.put("error", errorCode.getHttpStatus().getReasonPhrase());
    errorResponse.put("message", errorCode.getMessage());

    ObjectMapper objectMapper = new ObjectMapper();
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  private String getTokenFromHeader(HttpServletRequest request, String headerName) {
    String token = request.getHeader(headerName);
    if (token != null && token.startsWith("Bearer ")) {
      return token.substring(7);
    }
    return null;
  }

  private String reissueToken(
      String refreshToken, HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    try {
      Long memberId = redisService.get(refreshToken);
      Member member = memberService.findById(memberId);
      String newAccessToken = tokenProvider.generateAccessToken(member);
      log.info("재발급된 토큰 : " + newAccessToken);

      // 응답에 엑세스 토큰 추가 (헤더에 Authorization: Bearer {newAccessToken} 추가)
      response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
      return newAccessToken;
    } catch (MemberException e) {
      log.error("유효하지 않은 인증정보입니다.");
      handleAuthError(response, AuthErrorCode.TOKEN_EXPIRED);
      throw e;
    } catch (Exception e) {
      log.error("토큰 갱신 중 오류 발생: {}", e.getMessage());
      handleAuthError(response, AuthErrorCode.TOKEN_REISSUE_FAILED);
      throw e;
    }
  }
}
