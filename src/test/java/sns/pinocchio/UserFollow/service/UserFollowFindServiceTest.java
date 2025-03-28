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
		// Given
		String followingId = "user123";
		String followingNickname = "홍길동";
		String followerId = "user321";
		String followerNickname = "고길동";

		UserFollow userFollow = UserFollow.builder()
			.followerNickname(followerNickname)
			.followerId(followerId)
			.followingNickname(followingNickname)
			.followingId(followingId)
			.build();

		List<UserFollow> followings = List.of(userFollow,userFollow,userFollow);

		when(userFollowRepository.findAllByFollowingId(followingId)).thenReturn(followings);

		Map<String, Object> result = userFollowService.findFollowers(followingId);
		String meesage = (String)result.get("message");

		List<Map<String, String>> followers = (List<Map<String, String>>)result.get("followers");

		assertEquals("팔로워 조회에 성공하였습니다.",meesage);
		assertEquals(3,followers.size());
		verify(userFollowRepository, times(1)).findAllByFollowingId(followingId);

	}

	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로우_조회_테스트() {
		// Given
		String followingId = "user123";
		String followingNickname = "홍길동";
		String followerId = "user321";
		String followerNickname = "고길동";

		UserFollow userFollow = UserFollow.builder()
			.followerNickname(followerNickname)
			.followerId(followerId)
			.followingNickname(followingNickname)
			.followingId(followingId)
			.build();

		List<UserFollow> followers = List.of(userFollow,userFollow,userFollow);

		when(userFollowRepository.findAllByFollowerId(followerId)).thenReturn(followers);

		Map<String, Object> result = userFollowService.findFollowings(followerId);
		String meesage = (String)result.get("message");

		List<Map<String, String>> followings = (List<Map<String, String>>)result.get("followings");

		assertEquals("팔로잉 조회에 성공하였습니다.",meesage);
		assertEquals(3, followings.size());
		verify(userFollowRepository, times(1)).findAllByFollowerId(followerId);
	}
}
