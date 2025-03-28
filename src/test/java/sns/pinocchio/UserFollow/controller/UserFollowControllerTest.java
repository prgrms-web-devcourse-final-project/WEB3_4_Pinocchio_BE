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
		String userTsid = "user_002";
		String loginUserId = "user_001";
		Map<String, Object> response = Map.of("message", "팔로우에 성공하였습니다.", "followed", true);
		when(userFollowService.followingUser(userTsid,loginUserId) ).thenReturn(response);

		mockMvc.perform(post("/users/"+userTsid+"/follow").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우에 성공하였습니다."))
			.andExpect(jsonPath("$.followed").value(true))
			.andDo(print());
		System.out.println("✅ 유저 팔로우 성공");
	}

	//유저 팔로우 취소 테스트
	@Test
	void 유저_팔로우_취소_테스트() throws Exception {
		String userTsid = "user_002";
		String loginUserId = "user_001";
		Map<String, Object> response = Map.of("message", "팔로우 취소에 성공하였습니다.", "followed", false);
		when(userFollowService.followingUser(userTsid,loginUserId) ).thenReturn(response);

		mockMvc.perform(post("/users/"+userTsid+"/follow").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우 취소에 성공하였습니다."))
			.andExpect(jsonPath("$.followed").value(false))
			.andDo(print());
		System.out.println("✅ 유저 팔로우 취소 성공");
	}




}
