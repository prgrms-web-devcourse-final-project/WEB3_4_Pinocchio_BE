package sns.pinocchio.MemberFollow.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.member.MemberFollowService;
import sns.pinocchio.application.member.memberDto.MemberFollowRequest;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
@SpringBootTest
public class MemberFollowerServiceTest {
	@InjectMocks
	private MemberFollowService memberFollowService;

	@Mock
	private UserFollowRepository userFollowRepository;

	//유저 팔로우 테스트 메서드
	@Test
	void 유저_팔로우_테스트() {
		String userId = "user_002";
		String authorId = "user_001";
		String authorNickname = "홍길동";
		MemberFollowRequest request = MemberFollowRequest.builder().followingNickname("고길동").build();

		UserFollow userFollow = UserFollow.builder().followingId(userId).followerId(authorId).status(CancellState.ACTIVE).build();


		when(userFollowRepository.save(any(UserFollow.class))).thenReturn(userFollow);
		when(userFollowRepository.findByFollowerIdAndFollowingId(authorId,userId)).thenReturn(Optional.empty());


		Map<String, Object> response = memberFollowService.followingUser(request,userId,authorId,authorNickname);
		String message = (String)response.get("message");
		boolean followed = (boolean)response.get("followed");

		assertEquals("팔로우에 성공하였습니다.", message);
		assertEquals(true,followed);

		verify(userFollowRepository, times(1)).save(any(UserFollow.class));
		verify(userFollowRepository, times(1)).findByFollowerIdAndFollowingId(authorId,userId);
		System.out.println("✅ 팔로우 성공");

	}
	//유저 팔로우 취소 테스트 메서드
	@Test
	void 유저_팔로우_취소_테스트() {
		String userId = "user_002";
		String authorId = "user_001";
		String authorNickname = "홍길동";
		MemberFollowRequest request = MemberFollowRequest.builder().followingNickname("고길동").build();

		UserFollow userFollow = UserFollow.builder().followingId(userId).followerId(authorId).status(CancellState.ACTIVE).build();
		UserFollow userFollowCancel = UserFollow.builder().followingId(userId).followerId(authorId).status(CancellState.CANCELLED).build();



		when(userFollowRepository.save(any(UserFollow.class))).thenReturn(userFollowCancel);
		when(userFollowRepository.findByFollowerIdAndFollowingId(authorId,userId)).thenReturn(Optional.of(userFollow));


		Map<String, Object> response = memberFollowService.followingUser(request,userId,authorId,authorNickname);
		String message = (String)response.get("message");
		boolean followed = (boolean)response.get("followed");

		assertEquals("팔로우 취소에 성공하였습니다.", message);
		assertEquals(false,followed);

		verify(userFollowRepository, times(1)).save(any(UserFollow.class));
		verify(userFollowRepository, times(1)).findByFollowerIdAndFollowingId(authorId,userId);
		System.out.println("✅ 팔로우 성공");

	}
}
