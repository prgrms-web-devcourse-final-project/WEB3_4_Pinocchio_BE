package sns.pinocchio.infrastructure.persistence.redis.redisService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.infrastructure.persistence.redis.redisDao.RedisDao;

@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisDao redisDao;

    // 데이터 저장 (만료 시간 미설정)
    public void save(String key, String value) {
        redisDao.save(key, value);
    }

    // 데이터 저장 (만료 시간 설정)
    public void save(String key, String value, long expirationTime) {
        redisDao.save(key, value, expirationTime);
    }

    // 데이터 조회
    public String get(String key) {
        return redisDao.get(key);
    }

    // 키 존재 여부 확인
    public boolean exists(String key) {
        return redisDao.exists(key);
    }

    // 데이터 삭제
    public void delete(String key) {
        redisDao.delete(key);
    }

    // 만료 시간 설정
    public void setExpiration(String key, long expirationTimeInSeconds) {
        redisDao.setExpiration(key, expirationTimeInSeconds);
    }

    // 블랙리스트에 추가
    public void addBlackList(String refreshToken, long expirationTimeInSeconds) {
        redisDao.save(refreshToken, "blacklisted", expirationTimeInSeconds);
    }

    // 리프레시 토큰 유효성 체크
    public boolean isValidRefreshToken(String key) {
        return !"blacklisted".equals(redisDao.get(key));
    }
}