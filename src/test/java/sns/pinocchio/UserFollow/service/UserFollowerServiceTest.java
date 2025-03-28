package sns.pinocchio.UserFollow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.user.UserFollowRequest;
import sns.pinocchio.application.user.UserFollowService;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.domain.user.UserFollowStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@SpringBootTest
public class UserFollowerServiceTest {
	@InjectMocks
	private UserFollowService userFollowService;

	@Mock
	private UserFollowRepository userFollowRepository;

	//유저 팔로우 테스트 메서드
	@Test
	void 유저_팔로우_테스트() {
		String userId = "user_002";
		String authorId = "user_001";
		String authorNickname = "홍길동";
		UserFollowRequest request = UserFollowRequest.builder().followingNickname("고길동").build();

		UserFollow userFollow = UserFollow.builder().followingId(userId).followerId(authorId).status(UserFollowStatus.ACTIVE).build();


		when(userFollowRepository.save(any(UserFollow.class))).thenReturn(userFollow);
		when(userFollowRepository.findByFollowerIdAndFollowingId(authorId,userId)).thenReturn(Optional.empty());


		Map<String, Object> response = userFollowService.followingUser(request,userId,authorId,authorNickname);
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
		String userTsid = "user_002";
		String authorId = "user_001";
		String authorNickname = "홍길동";
		UserFollowRequest request = UserFollowRequest.builder().followingNickname("고길동").build();

		UserFollow userFollow = UserFollow.builder().followingId(userTsid).followerId(authorId).status(UserFollowStatus.ACTIVE).build();
		UserFollow userFollowCancel = UserFollow.builder().followingId(userTsid).followerId(authorId).status(UserFollowStatus.DELETE).build();



		when(userFollowRepository.save(any(UserFollow.class))).thenReturn(userFollowCancel);
		when(userFollowRepository.findByFollowerIdAndFollowingId(authorId,userTsid)).thenReturn(Optional.of(userFollow));


		Map<String, Object> response = userFollowService.followingUser(request,userTsid,authorId,authorNickname);
		String message = (String)response.get("message");
		boolean followed = (boolean)response.get("followed");

		assertEquals("팔로우 취소에 성공하였습니다.", message);
		assertEquals(false,followed);

		verify(userFollowRepository, times(1)).save(any(UserFollow.class));
		verify(userFollowRepository, times(1)).findByFollowerIdAndFollowingId(authorId,userTsid);
		System.out.println("✅ 팔로우 성공");

	}
}
