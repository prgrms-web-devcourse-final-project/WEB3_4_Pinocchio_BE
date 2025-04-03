package sns.pinocchio.config.global.redis.redisDao;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RedisDao {

  private final RedisTemplate<String, String> redisTemplate;

  // 데이터 저장 (만료 시간 설정 없이)
  public void save(String key, String value) {
    redisTemplate.opsForValue().set(key, value);
  }

  // 데이터 저장 (만료 시간 설정 포함)
  public void save(String key, String value, long expirationTime) {
    redisTemplate.opsForValue().set(key, value, expirationTime, TimeUnit.SECONDS);
  }

  // 데이터 조회
  public String get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  // 데이터 존재 여부 확인
  public boolean exists(String key) {
    return redisTemplate.hasKey(key);
  }

  // 데이터 삭제
  public void delete(String key) {
    redisTemplate.delete(key);
  }

  // 만료 시간 갱신 (시간 단위는 초)
  public void setExpiration(String key, long expirationTimeInSeconds) {
    redisTemplate.expire(key, expirationTimeInSeconds, TimeUnit.SECONDS);
  }
}
