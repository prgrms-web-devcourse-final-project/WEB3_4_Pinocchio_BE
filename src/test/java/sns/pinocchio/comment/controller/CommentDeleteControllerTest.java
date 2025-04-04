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
import sns.pinocchio.application.comment.commentDto.CommentDeleteRequest;
import sns.pinocchio.presentation.comment.CommentController;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@WebMvcTest(CommentController.class)
class CommentDeleteControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	//댓글 삭제 테스트 
	@Test
	void 댓글_삭제_테스트() throws Exception {
		String commentId = "comment_001";
		String postId = "post_001";

		CommentDeleteRequest request = CommentDeleteRequest.builder().commentId(commentId).postId(postId).build();
		Map<String, Object> response = Map.of("message", "댓글이 삭제되었습니다.");

		when(commentService.deleteComment(any(CommentDeleteRequest.class))).thenReturn(response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(false);

		mockMvc.perform(delete("/comments").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."))
			.andDo(print());
		System.out.println("✅ 댓글 삭제 성공");
	}

	//댓글 삭제 실패 테스트 댓글없음
	@Test
	void 댓글_삭제_실패_테스트_댓글없음() throws Exception {
		String commentId = "comment_001";
		String postId = "post_001";

		CommentDeleteRequest request = CommentDeleteRequest.builder().commentId(commentId).postId(postId).build();

		when(commentService.isInvalidComment(commentId, postId)).thenReturn(true);

		mockMvc.perform(delete("/comments").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("등록된 댓글을 찾을 수 없습니다."))
			.andDo(print());
		System.out.println("✅ 댓글 삭제 실패 성공");

	}

}
