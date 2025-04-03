package sns.pinocchio.comment.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import sns.pinocchio.application.comment.commentDto.CommentModifyRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.presentation.comment.CommentController;

@WebMvcTest(CommentController.class)

public class CommentModifyControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	//댓글 수정 테스트
	@Test
	public void 댓글_수정_테스트() throws Exception {
		String commetId = "comment_001";
		String postId = "post_001";

		CommentModifyRequest request = CommentModifyRequest.builder()
			.commentId(commetId)
			.postId(postId)
			.content("수정된 댓글 내용")
			.build();
		Map<String, Object> response = Map.of("message", "댓글이 성공적으로 수정되었습니다.", "postId", postId, "commentId", commetId,
			"updatedAt", LocalDateTime.now().toString());

		when(commentService.modifyComment(any(CommentModifyRequest.class))).thenReturn(response);
		mockMvc.perform(put("/comments/modify").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글이 성공적으로 수정되었습니다."))
			.andExpect(jsonPath("$.commentId").value(commetId))
			.andDo(print());

	}

	//댓글 수정 실패 테스트 댓글없음
	@Test
	public void 댓글_수정_테스트_실패_댓글없음() throws Exception {
		String commentId = "comment_001";
		String postId = "post_001";

		CommentModifyRequest request = CommentModifyRequest.builder()
			.commentId(commentId)
			.postId(postId)
			.content("수정된 댓글 내용")
			.build();
		Map<String, Object> response = Map.of("message", "댓글이 성공적으로 수정되었습니다.", "postId", postId, "commentId", commentId,
			"updatedAt", LocalDateTime.now().toString());

		when(commentService.modifyComment(any(CommentModifyRequest.class))).thenReturn(response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(true);

		mockMvc.perform(put("/comments/modify").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("등록된 댓글을 찾을 수 없습니다."))
			.andDo(print());

	}
}
