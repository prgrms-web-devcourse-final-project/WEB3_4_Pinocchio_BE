package sns.pinocchio.config.global.auth.util;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sns.pinocchio.domain.member.Member;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {

  private final JwtUtil jwtUtil;

  // JWT 토큰 생성
  public String generateAccessToken(Member member) {
    // TODO : 오류 해결 후 log 삭제
    log.info("jwt 토큰 생성 시작");
    return Jwts.builder()
        .setSubject(member.getName()) // 사용자 이름
        .claim("id", member.getId()) // 사용자 ID
        .claim("email", member.getEmail()) // 사용자 이메일
        .claim("tsid", member.getTsid())
        .setIssuedAt(new Date()) // 토큰 발급 시간
        .setExpiration(
            new Date(System.currentTimeMillis() + jwtUtil.getAccessTokenExpirationTime())) // 만료시간
        .signWith(jwtUtil.getKey()) // 서명 알고리즘 및 키
        .compact();
  }

  // 리프레시 토큰 생성
  public String generateRefreshToken() {
    return UUID.randomUUID().toString();
  }

  // 리프레시 토큰 만료 시간 계산
  public LocalDateTime getRefreshTokenExpiryDate() {
    return LocalDateTime.now().plus(Duration.ofMillis(jwtUtil.getRefreshTokenExpirationTime()));
  }
}
