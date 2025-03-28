package sns.pinocchio.application.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.domain.user.UserFollowStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@Service
@RequiredArgsConstructor
public class UserFollowService {
	private final UserFollowRepository userFollowRepository;

	public Map<String, Object> followingUser(UserFollowRequest request, String followingId, String authorId,
		String authorNickname) {
		Optional<UserFollow> optUserFollow = userFollowRepository.findByFollowerIdAndFollowingId(authorId, followingId);

		if (optUserFollow.isEmpty()) {
			UserFollow newUserFollow = UserFollow.builder()
				.followingId(authorId)
				.followingNickname(request.followingNickname)
				.followerId(followingId)
				.followerNickname(authorNickname)
				.status(UserFollowStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.build();
			userFollowRepository.save(newUserFollow);
			return Map.of("message", "팔로우에 성공하였습니다.", "followed", true);
		} else {
			UserFollow userFollow = optUserFollow.get();
			boolean isActive = userFollow.getStatus() == UserFollowStatus.ACTIVE;
			userFollow.setStatus(isActive ? UserFollowStatus.DELETE : UserFollowStatus.ACTIVE);
			userFollowRepository.save(userFollow);
			return Map.of("message", isActive ? "팔로우 취소에 성공하였습니다." : "팔로우에 성공하였습니다.", "followed",
				isActive ? false : true);
		}
	}

	public Map<String, Object> findFollowers(String followingId) {
		List<Map<String, String>> followers = userFollowRepository.findAllByFollowingId(followingId)
			.stream()
			.map(userFollow -> {
				String followerId = userFollow.getFollowerId();
				String nickname = userFollow.getFollowerNickname();
				return Map.of("userId", followerId, "nickname", nickname);
			})
			.toList();

		return Map.of("message", "팔로워 조회에 성공하였습니다.", "followers", followers);
	}

	public Map<String, Object> findFollowings(String followerId) {
		List<Map<String, String>> followings = userFollowRepository.findAllByFollowerId(followerId)
			.stream()
			.map(userFollow -> {
				String followingId = userFollow.getFollowingId();
				String nickname = userFollow.getFollowingNickname();
				return Map.of("userId", followingId, "nickname", nickname);
			})
			.toList();

		return Map.of("message", "팔로잉 조회에 성공하였습니다.", "followings", followings);
	}
}
