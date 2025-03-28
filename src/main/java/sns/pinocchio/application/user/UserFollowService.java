package sns.pinocchio.application.user;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.comment.CommentLikeService;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.domain.user.UserFollowStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@Service
@RequiredArgsConstructor
public class UserFollowService {
	private final UserFollowRepository userFollowRepository;

	public Map<String, Object> followingUser(String followingId, String authorId) {
		Optional<UserFollow> optUserFollow = userFollowRepository.findByFollowerIdAndFollowingId(authorId,
			followingId);

		if (optUserFollow.isEmpty()) {
			UserFollow newUserFollow = UserFollow.builder()
				.followingId(authorId)
				.followerId(followingId)
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
			return Map.of(
				"message", isActive ? "팔로우 취소에 성공하였습니다." : "팔로우에 성공하였습니다.",
				"followed", isActive ? false : true
			);
		}
	}
}
