package sns.pinocchio.config.global.auth.jwt;

import com.fasterxml.jackson.core.ObjectCodec;
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
import sns.pinocchio.config.global.auth.util.JwtUtil;
import sns.pinocchio.presentation.auth.exception.AuthErrorCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberAuthFilter extends OncePerRequestFilter {

  private final MemberService memberService;
  private final CustomUserDetailService customUserDetailService;
  private final JwtUtil jwtUtil;

  ObjectCodec objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String accessToken = getTokenFromHeader(request, HttpHeaders.AUTHORIZATION);

    // 토큰이 없는 경우 바로 다음 필터로 진행
    if (accessToken == null) {
      log.debug("인증 토큰이 없습니다.");
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
          handleAuthError(response, AuthErrorCode.TOKEN_EXPIRED);
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

  // 사용자 권한이 필요한 api 경로만 필터링을 하는 메서드
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {

    String path = request.getRequestURI();
    String method = request.getMethod();
    log.info("요청 경로: {}, 메서드: {}", path, method);

    boolean shouldSkip =
        ((method.equals("GET") && path.equals("/api/posts/search"))
            || (method.equals("POST") && path.equals("/api/auth/signup"))
            || (method.equals("POST") && path.equals("/api/auth/login"))
            || (method.equals("POST") && path.equals("/api/auth/logout"))
            || path.startsWith("/swagger")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/swagger-resources")
            || path.startsWith("/webjars"));
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
}
