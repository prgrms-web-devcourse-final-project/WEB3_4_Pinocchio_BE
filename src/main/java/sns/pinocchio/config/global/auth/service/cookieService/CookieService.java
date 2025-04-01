package sns.pinocchio.config.global.auth.service.cookieService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.auth.util.CookieUtil;

@Service
@RequiredArgsConstructor
public class CookieService {

  private final CookieUtil cookieUtil;

  public void addRefreshTokenToCookie(String refreshToken, Long ExpirationTime, HttpServletResponse response) {
    cookieUtil.addTokenToCookie(
        "refreshToken", refreshToken, ExpirationTime, response);
  }

  public String getRefreshTokenFromCookie(HttpServletRequest request) {
    return cookieUtil.getTokenFromCookie("refreshToken", request);
  }

  public void clearTokenFromCookie(HttpServletResponse response) {
    cookieUtil.addTokenToCookie("refreshToken", null, 0, response);
  }
}
