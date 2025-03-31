package sns.pinocchio.config.global.redis.redisService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.redis.redisDao.RedisDao;

@RequiredArgsConstructor
@Service
public class RedisService {

  private final RedisDao redisDao;

  public void save(String key, String value) {
    redisDao.save(key, value);
  }

  public void save(String key, String value, long expirationTime) {
    redisDao.save(key, value, expirationTime);
  }

  public String get(String key) {
    return redisDao.get(key);
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
    redisDao.save(refreshToken, "blacklisted", expirationTimeInSeconds);
  }

  // 리프레시 토큰이 유효한지 확인하는 메서드
  public boolean isValidRefreshToken(String key) {
    return !"blacklisted".equals(redisDao.get(key));
  }
}
