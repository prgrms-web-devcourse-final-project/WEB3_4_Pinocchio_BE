package sns.pinocchio.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentLikeRequest;
import sns.pinocchio.presentation.comment.CommentController;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(CommentController.class)
public class CommentLikeControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	//댓글 좋아요 테스트
	@Test
	public void 댓글_좋아요_테스트() throws Exception {
		String postId = "post_001";
		String commentId = "comment_001";
		CommentLikeRequest request = CommentLikeRequest.builder().postId(postId).build();
		Map<String, Object> response = Map.of("message", "좋아요 요청에 성공했습니다.", "userId", "user_001", "liked", true,
			"likes", 1);

		when(commentService.toggleCommentLike(any(CommentLikeRequest.class), anyString(), anyString())).thenReturn(
			response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(false);
		mockMvc.perform(post("/comments/comment_001/like").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("좋아요 요청에 성공했습니다."))
			.andExpect(jsonPath("$.likes").value(1))
			.andExpect(jsonPath("$.liked").value(true))
			.andDo(print());

	}

	//댓글 좋아요 실패 테스트
	public void 댓글_좋아요_실패_테스트() throws Exception {
		String postId = "post_001";
		String commentId = "comment_001";

		CommentLikeRequest request = CommentLikeRequest.builder().postId(postId).build();
		Map<String, Object> response = Map.of("message", "좋아요 요청에 성공했습니다.", "userId", "user_001", "liked", true,
			"likes", 1);

		when(commentService.toggleCommentLike(any(CommentLikeRequest.class), anyString(), anyString())).thenReturn(
			response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(true);

		mockMvc.perform(post("/comments/comment_001/like").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("등록된 댓글을 찾을 수 없습니다."))
			.andDo(print());

	}
}
