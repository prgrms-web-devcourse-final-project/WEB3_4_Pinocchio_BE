package sns.pinocchio.config.global.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sns.pinocchio.config.global.auth.jwt.TokenStatus;

@Component
public class JwtUtil {

  @Value("${spring.security.jwt.secret-key}")
  private String SECRET_KEY;

  @Value("${spring.security.jwt.access-token.expiration}")
  private long ACCESS_TOKEN_EXPIRATION_TIME; // 6시간 (단위: ms)

  @Value("${spring.security.jwt.refresh-token.expiration}")
  private long REFRESH_TOKEN_EXPIRATION_TIME; // 60일(약 2달) (단위: ms)

  private static Key key;

  private static JwtUtil instance;

  public Long getAccessTokenExpirationTime() {
    return ACCESS_TOKEN_EXPIRATION_TIME;
  }

  public Long getRefreshTokenExpirationTime() {
    return REFRESH_TOKEN_EXPIRATION_TIME;
  }

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }

  public Key getKey() {
    return key;
  }

  // 토큰 검증 메서드
  public static TokenStatus validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return TokenStatus.VALID; // 유효한 토큰
    } catch (ExpiredJwtException e) { // 토큰이 만료됨
      return TokenStatus.EXPIRED;
    } catch (MalformedJwtException e) { // 토큰이 올바르지 않음
      return TokenStatus.MALFORMED;
    } catch (IllegalArgumentException e) { // 토큰이 비었거나 올바르지 않음
      return TokenStatus.INVALID;
    }
  }

  // 토큰으로 유저 정보 가져오기
  public static Long getMember(String token) {
    Claims claims = parseToken(token);
    return claims.get("id", Long.class);
  }

  // JWT 검증 및 정보 추출
  public static Claims parseToken(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }
}
