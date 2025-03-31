package sns.pinocchio.UserFollow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import sns.pinocchio.application.user.UserFollowRequest;
import sns.pinocchio.application.user.UserFollowService;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.domain.user.UserFollowStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@SpringBootTest
public class UserFollowFindServiceTest {
	@Mock
	private UserFollowRepository userFollowRepository;

	@InjectMocks
	private UserFollowService userFollowService;

	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로워_조회_테스트() {
		String followingId = "user123";
		String followingNickname = "홍길동";
		String followerId = "user321";
		String followerNickname = "고길동";
		int page = 0;
		Pageable pageable = PageRequest.of(page, 10);

		UserFollow userFollow = UserFollow.builder()
			.followerNickname(followerNickname)
			.followerId(followerId)
			.followingNickname(followingNickname)
			.followingId(followingId)
			.build();

		Page<UserFollow> followingsPage = new PageImpl<>(List.of(userFollow, userFollow, userFollow));

		when(userFollowRepository.findAllByFollowingIdAndStatusOrderByUpdatedAtDesc(followingId, pageable,
			UserFollowStatus.ACTIVE)).thenReturn(followingsPage);

		Map<String, Object> result = userFollowService.findFollowers(followingId, page);
		String message = (String)result.get("message");

		List<Map<String, String>> followers = (List<Map<String, String>>)result.get("followers");

		assertEquals("팔로워 조회에 성공하였습니다.", message);
		assertEquals(3, followers.size());
		verify(userFollowRepository, times(1)).findAllByFollowingIdAndStatusOrderByUpdatedAtDesc(followingId, pageable,
			UserFollowStatus.ACTIVE); // Verify that the method was called with pageable
	}

	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로우_조회_테스트() {
		// Given
		String followingId = "user123";
		String followingNickname = "홍길동";
		String followerId = "user321";
		String followerNickname = "고길동";
		int page = 0;
		Pageable pageable = PageRequest.of(page, 10);

		UserFollow userFollow = UserFollow.builder()
			.followerNickname(followerNickname)
			.followerId(followerId)
			.followingNickname(followingNickname)
			.followingId(followingId)
			.build();

		Page<UserFollow> followingsPage = new PageImpl<>(List.of(userFollow, userFollow, userFollow));

		when(userFollowRepository.findAllByFollowerIdAndStatusOrderByUpdatedAtDesc(followerId, pageable,
			UserFollowStatus.ACTIVE)).thenReturn(followingsPage);

		Map<String, Object> result = userFollowService.findFollowings(followerId, page);
		String meesage = (String)result.get("message");

		List<Map<String, String>> followings = (List<Map<String, String>>)result.get("followings");

		assertEquals("팔로잉 조회에 성공하였습니다.", meesage);
		assertEquals(3, followings.size());
		verify(userFollowRepository, times(1)).findAllByFollowerIdAndStatusOrderByUpdatedAtDesc(followerId, pageable,
			UserFollowStatus.ACTIVE);
	}
}
