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

import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.presentation.user.UserInfoFindController;

@WebMvcTest(UserInfoFindController.class)
public class UserFindCommentsControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	//유저 댓글 목록 조회
	@Test
	void 유저_댓글_목록_조회() throws Exception {
		String authorId = "user_001";
		int page = 0;

		Map<String,String> info = Map.of("id", "1");
		List<Map<String, String>> comments = List.of(info,info,info,info,info);
		Map<String, Object> response = Map.of("message","댓글요청에 성공하였습니다.","comments", comments);
		when(commentService.findCommentsByUser(authorId,page)).thenReturn(response);
		mockMvc.perform(get("/users/"+authorId+"/activities/comments").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글요청에 성공하였습니다."))
			.andExpect(jsonPath("$.comments.length()").value(5))
			.andDo(print());
		System.out.println("✅ 유저 댓글 조회 성공");
	}
}
