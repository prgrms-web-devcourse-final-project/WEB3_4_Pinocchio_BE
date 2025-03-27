package sns.pinocchio.config.global.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.auth.util.CookieUtil;
import sns.pinocchio.config.global.auth.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;

    // 엑세스토큰 저장
    public void addAccessTokenToCookie(String accessToken, HttpServletResponse response) {
        cookieUtil.addTokenToCookie("accessToken", accessToken,
                jwtUtil.getAccessTokenExpirationTime(), response);
    }

    // 리프레시토큰 저장
    public void addRefreshTokenToCookie(String refreshToken, HttpServletResponse response) {
        cookieUtil.addTokenToCookie("refreshToken", refreshToken,
                jwtUtil.getRefreshTokenExpirationTime(), response);
    }

    // 쿠키에서 엑세스토큰 추출
    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return cookieUtil.getTokenFromCookie("accessToken", request);
    }

    // 쿠키에서 리프레시토큰 추출
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return cookieUtil.getTokenFromCookie("refreshToken", request);
    }

    // 토큰 만료
    public void clearTokenFromCookie(HttpServletResponse response) {
        cookieUtil.addTokenToCookie("accessToken", null, 0, response);
        cookieUtil.addTokenToCookie("refreshToken", null, 0, response);
    }
}