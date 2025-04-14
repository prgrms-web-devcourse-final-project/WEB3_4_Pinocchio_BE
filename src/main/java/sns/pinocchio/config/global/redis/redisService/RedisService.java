package sns.pinocchio.config.global.redis.redisService;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.redis.redisDao.RedisDao;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisService {

  private final RedisDao redisDao;

  public void save(String key, String value, long expirationTime) {
    redisDao.save(key, value, expirationTime);
  }

  public Long get(String key) {
    return Long.valueOf(redisDao.get(key));
  }

  public boolean exists(String key) {
    return redisDao.exists(key);
  }

  public void delete(String key) {
    redisDao.delete(key);
  }

  public void setExpiration(String key, long expirationTimeInSeconds) {
    redisDao.setExpiration(key, expirationTimeInSeconds);
  }

  public boolean isExistsRefreshToken(String refreshToken) {
    return redisDao.exists(refreshToken);
  }

    // 블랙리스트에 추가하는 메서드
    public void addBlackList(String refreshToken, long expirationTimeInSeconds) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.warn("🚨 블랙리스트 등록 실패: refreshToken이 null이거나 비어있습니다.");
            return;
        }

        redisDao.save(refreshToken, "blacklisted", expirationTimeInSeconds);
        log.info("✅ 블랙리스트 등록 완료 | token 앞 10자리: {}", refreshToken.substring(0, Math.min(10, refreshToken.length())));
    }


  // 리프레시 토큰이 유효한지 확인하는 메서드
  public boolean isValidRefreshToken(String key) {
    return !"blacklisted".equals(redisDao.get(key));
  }
}
