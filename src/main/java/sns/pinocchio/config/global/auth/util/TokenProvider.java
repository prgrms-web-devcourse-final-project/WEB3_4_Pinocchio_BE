package sns.pinocchio.config.global.auth.util;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sns.pinocchio.domain.member.Member;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TokenProvider {

  private final JwtUtil jwtUtil;

  public String generateAccessToken(Member member) {
    return Jwts.builder()
        .setSubject(member.getName())
        .claim("id", member.getId())
        .claim("email", member.getEmail())
        .claim("tsid", member.getTsid())
        .setIssuedAt(new Date())
        .setExpiration(
            new Date(System.currentTimeMillis() + jwtUtil.getAccessTokenExpirationTime()))
        .signWith(jwtUtil.getKey()) // 서명 알고리즘 및 키
        .compact();
  }

  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  public LocalDateTime getRefreshTokenExpiryDate() {
    return LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshTokenExpirationTime()));
  }
}
