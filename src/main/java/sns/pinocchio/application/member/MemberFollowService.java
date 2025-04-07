package sns.pinocchio.application.member;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.member.memberDto.MemberFollowRequest;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.domain.user.UserFollowStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@Service
@RequiredArgsConstructor
public class MemberFollowService {
	private final UserFollowRepository userFollowRepository;

	public Map<String, Object> followingUser(MemberFollowRequest request, String followingId, String authorId,
		String authorNickname) {
		Optional<UserFollow> optUserFollow = userFollowRepository.findByFollowerIdAndFollowingId(authorId, followingId);

		if (optUserFollow.isEmpty()) {
			UserFollow newUserFollow = UserFollow.builder()
				.followingId(followingId)
				.followingNickname(request.getFollowingNickname())
				.followerId(authorId)
				.followerNickname(authorNickname)
				.status(UserFollowStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
			userFollowRepository.save(newUserFollow);
			return Map.of("message", "팔로우에 성공하였습니다.", "followed", true);
		} else {
			UserFollow userFollow = optUserFollow.get();
			boolean isActive = userFollow.toggleFollowStatus();
			userFollow.setUpdatedAt(LocalDateTime.now());
			userFollowRepository.save(userFollow);
			if (isActive) {
				return Map.of("message", "팔로우에 성공하였습니다.",
					"followed", true);
			}
			return Map.of("message", "팔로우 취소에 성공하였습니다.",
				"followed", false);

		}
	}

	public Map<String, Object> findFollowers(String followingId, int page) {
		Pageable pageable = PageRequest.of(page, 15);
		Page<UserFollow> userFollowPage = userFollowRepository.findAllByFollowingIdAndStatusOrderByUpdatedAtDesc(
			followingId, pageable, UserFollowStatus.ACTIVE);
		List<Map<String, Object>> followers = userFollowPage.getContent().stream().map(userFollow -> {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userFollow.getFollowerId());
			map.put("nickname", userFollow.getFollowerNickname());
			return map;
		}).toList();
		long totalElements = userFollowPage.getTotalElements();
		long totalPages = userFollowPage.getTotalPages();

		return Map.of("message", "팔로워 조회에 성공하였습니다.",
			"page", page,
			"totalElements", totalElements,
			"totalPages", totalPages,
			"followers", followers);
	}

	public Map<String, Object> findFollowings(String followerId, int page) {
		Pageable pageable = PageRequest.of(page, 15);
		Page<UserFollow> userFollowPage = userFollowRepository.findAllByFollowerIdAndStatusOrderByUpdatedAtDesc(
			followerId, pageable, UserFollowStatus.ACTIVE);
		List<Map<String, Object>> followings = userFollowPage.getContent().stream().map(userFollow -> {
			Map<String, Object> map = new HashMap<>();
			map.put("userId", userFollow.getFollowingId());
			map.put("nickname", userFollow.getFollowingNickname());
			return map;
		}).toList();

		long totalElements = userFollowPage.getTotalElements();
		long totalPages = userFollowPage.getTotalPages();

		return Map.of("message", "팔로잉 조회에 성공하였습니다.",
			"page", page,
			"totalElements", totalElements,
			"totalPages", totalPages,
			"followings", followings);
	}
}
