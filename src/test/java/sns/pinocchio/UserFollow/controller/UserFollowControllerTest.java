package sns.pinocchio.UserFollow.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sns.pinocchio.application.user.UserFollowRequest;
import sns.pinocchio.application.user.UserFollowService;
import sns.pinocchio.presentation.user.UserFollowController;

@WebMvcTest(UserFollowController.class)
class UserFollowControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserFollowService userFollowService;

	//유저 팔로우 테스트
	@Test
	void 유저_팔로우_테스트() throws Exception {
		String userId = "user_002";
		String authorId = "user_001";
		String authorNickname = "고길동";
		UserFollowRequest request = UserFollowRequest.builder().followingNickname("홍길동").build();
		Map<String, Object> response = Map.of("message", "팔로우에 성공하였습니다.", "followed", true);
		when(userFollowService.followingUser(request,userId,authorId,authorNickname) ).thenReturn(response);

		mockMvc.perform(post("/users/"+userId+"/follow").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우에 성공하였습니다."))
			.andExpect(jsonPath("$.followed").value(true))
			.andDo(print());
		System.out.println("✅ 유저 팔로우 성공");
	}

	//유저 팔로우 취소 테스트
	@Test
	void 유저_팔로우_취소_테스트() throws Exception {
		String userId = "user_002";
		String authorId = "user_001";
		String authorNickname = "고길동";
		UserFollowRequest request = UserFollowRequest.builder().followingNickname("홍길동").build();
		Map<String, Object> response = Map.of("message", "팔로우 취소에 성공하였습니다.", "followed", false);
		when(userFollowService.followingUser(request,userId,authorId,authorNickname) ).thenReturn(response);

		mockMvc.perform(post("/users/"+userId+"/follow").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우 취소에 성공하였습니다."))
			.andExpect(jsonPath("$.followed").value(false))
			.andDo(print());
		System.out.println("✅ 유저 팔로우 취소 성공");
	}




}
