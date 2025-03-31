package sns.pinocchio.UserFollow.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sns.pinocchio.application.user.UserFollowRequest;
import sns.pinocchio.application.user.UserFollowService;
import sns.pinocchio.presentation.user.UserFollowController;

@WebMvcTest(UserFollowController.class)
class UserFollowFIndControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserFollowService userFollowService;

	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로워_조회_테스트() throws Exception {
		String followingId = "user_002";
		String followingNickname = "홍길동";
		int page = 0;


		Map<String,String> info = Map.of("userId", followingId, "nickname", followingNickname);
		List<Map<String, String>> followings = List.of(info,info,info,info,info);

		Map<String, Object> response = Map.of("message", "팔로워 조회에 성공하였습니다.", "followers", followings);
		when(userFollowService.findFollowers(followingId,page) ).thenReturn(response);

		mockMvc.perform(post("/users/"+followingId+"/followers").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로워 조회에 성공하였습니다."))
			.andExpect(jsonPath("$.followers.length()").value(5))
			.andDo(print());
		System.out.println("✅ 유저 팔로워 조회 성공");
	}

	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로잉_조회_테스트() throws Exception {
		String followerId = "user_002";
		String followerNickname = "홍길동";
		int page = 0;

		Map<String,String> info = Map.of("userId", followerId, "nickname", followerNickname);
		List<Map<String, String>> followers = List.of(info,info,info,info,info);

		Map<String, Object> response = Map.of("message", "팔로잉 조회에 성공하였습니다.", "followings", followers);
		when(userFollowService.findFollowings(followerId,page) ).thenReturn(response);

		mockMvc.perform(post("/users/"+followerId+"/followings").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로잉 조회에 성공하였습니다."))
			.andExpect(jsonPath("$.followings.length()").value(5))
			.andDo(print());
		System.out.println("✅ 유저 팔로잉 조회 성공");
	}
}
