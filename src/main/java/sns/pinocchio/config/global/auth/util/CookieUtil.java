package sns.pinocchio.config.global.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

  public void addTokenToCookie(
      String name, String value, long expiration, HttpServletResponse response) {
    ResponseCookie cookie =
        ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(expiration)
            .build();

    response.addHeader("Set-Cookie", cookie.toString());
  }

  public void clearTokenFromCookie(HttpServletResponse response) {
    ResponseCookie expiredCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("Strict")
            .maxAge(0)
            .build();

    response.addHeader("Set-Cookie", expiredCookie.toString());
  }

  public String getTokenFromCookie(String name, HttpServletRequest request) {
    if (request.getCookies() == null) return null;

    return Arrays.stream(request.getCookies())
        .filter(cookie -> name.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
