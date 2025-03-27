package sns.pinocchio.application.user;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@Service
@RequiredArgsConstructor
public class UserFollowService {
	private UserFollowRepository userFollowRepository;

	public Map<String, Object> followingUser(String userId, String loginUserId) {
		Optional<UserFollow> optUserFollow = userFollowRepository.findByFollowerIdAndFollowingId(loginUserId, userId);

		if (optUserFollow.isEmpty()) {
			UserFollow newUserFollow = UserFollow.builder()
				.followingId(loginUserId)
				.followerId(userId)
				.createdAt(LocalDateTime.now())
				.build();
			userFollowRepository.save(newUserFollow);
			return Map.of("message", "팔로우에 성공하였습니다.", "followed", true);
		} else {
			userFollowRepository.delete(optUserFollow.get());
			return Map.of("message", "팔로우 취소에 성공하였습니다.", "followed", false);
		}

	}
}
