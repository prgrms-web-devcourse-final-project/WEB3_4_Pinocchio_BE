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
import sns.pinocchio.config.global.redis.redisService.RedisService;
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
    final String uri = request.getRequestURI();

    // 토큰이 없는 경우 바로 다음 필터로 진행
    if (accessToken == null) {
        log.warn("🚫 인증 토큰 없음 | URI: {}", uri);
      handleAuthError(response, AuthErrorCode.INVALID_TOKEN);
      return;
    }

    try {
      TokenStatus tokenStatus = JwtUtil.validateToken(accessToken);

      switch (tokenStatus) {
        case VALID:
          log.info("✅ 유효한 토큰 | URI: {} | Token(앞 10자리): {}...", uri, accessToken.substring(0, 10));
          setAuthenticationInContext(accessToken);
          break;

        case EXPIRED:
          log.warn("⚠️ 만료된 토큰 | URI: {} | 재발급 시도", uri);
          String newAccessToken = reissueToken(refreshToken, request, response);
          log.info("♻️ 토큰 재발급 성공 | URI: {} | NewToken(앞 10자리): {}...", uri, newAccessToken.substring(0, 10));
          setAuthenticationInContext(newAccessToken);
          break;

        case MALFORMED, INVALID:
          log.error("❌ 잘못된 토큰 | URI: {} | Token: {}", uri, accessToken);
          handleAuthError(response, AuthErrorCode.INVALID_TOKEN);
          return;
      }
    } catch (Exception e) {
      log.error("🔥 예외 발생 | URI: {} | 메시지: {}", request.getRequestURI(), e.getMessage());
      handleAuthError(response, AuthErrorCode.AUTHORIZATION_FAILED);
      return;
    }
    filterChain.doFilter(request, response);
  }

  // 사용자 권한이 필요한 api 경로만 필터링을 하는 메서드
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

      String path = request.getRequestURI();
      String method = request.getMethod();
      log.info("요청 경로: {}, 메서드: {}", path, method);

      boolean shouldSkip =
              // === [ 인증 예외 API ] ===
                      (method.equals("GET") && path.startsWith("/actuator/health"))
                      || (method.equals("POST") && path.startsWith("/auth"))
                      || (method.equals("POST") && path.startsWith("/user/password/reset"))

                      // === [ Swagger 문서 예외 ] ===
                      || path.startsWith("/swagger")
                      || path.startsWith("/v3/api-docs")
                      || path.startsWith("/swagger-ui")
                      || path.startsWith("/swagger-resources")
                      || path.startsWith("/webjars")

                      // === [ 정적 리소스 및 React Router 경로 예외 ] ===
                      || path.equals("/")
                      || path.equals("/index.html")
                      || path.startsWith("/static/")
                      || path.startsWith("/favicon.ico")
                      || path.startsWith("/manifest.json")
                      || path.startsWith("/asset-manifest.json")
                      || path.startsWith("/logo")
                      || path.equals("/login")         // ✅ React SPA 로그인 경로
                      || path.equals("/signup")        // ✅ React SPA 회원가입 경로
                      || path.equals("/main")
                      || path.startsWith("/board")
                      || path.startsWith("/mypage")
                      || path.matches("/[a-zA-Z0-9\\-_/]*\\.js")
                      || path.matches("/[a-zA-Z0-9\\-_/]*\\.css")
                      || path.matches("/[a-zA-Z0-9\\-_/]*\\.woff2")
                      || path.matches("/[a-zA-Z0-9\\-_/]*\\.svg");

      log.info("필터 건너뛰기: {}", shouldSkip);
      return shouldSkip;
  }

  // SecurityContext에 인증 정보를 주입하는 메서드
  private void setAuthenticationInContext(String accessToken) {
    CustomUserDetails customUserDetails =
        customUserDetailService.loadUserByAccessToken(accessToken);
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            customUserDetails, null, customUserDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
  }

  // 인증 오류를 직접 HTTP 응답으로 처리하는 메서드 (예외를 던지지 않음)
  private void handleAuthError(HttpServletResponse response, AuthErrorCode errorCode)
      throws IOException {
    SecurityContextHolder.clearContext();

    response.setStatus(errorCode.getHttpStatus().value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    // 에러 응답 JSON 생성
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("status", errorCode.getHttpStatus().value());
    errorResponse.put("error", errorCode.getHttpStatus().getReasonPhrase());
    errorResponse.put("message", errorCode.getMessage());

    // JSON 문자열로 변환하여 응답
    ObjectMapper objectMapper = new ObjectMapper();
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }

  // 헤더에서 토큰 가져오는 메서드
  private String getTokenFromHeader(HttpServletRequest request, String headerName) {
    String token = request.getHeader(headerName);
    if (token != null && token.startsWith("Bearer ")) {
      return token.substring(7);
    }
    return null;
  }

  // 엑세스 토큰 재발급
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
