package sns.pinocchio.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentDeleteRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.DeleteType;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentFIndServiceTest {
	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentRepository commentRepository;

	//댓글 조회 테스트 게시글에서 조회했는데 댓글이 하나도 안달린 상황
	@Test
	public void 게시글_댓글_조회_테스트_댓글없음() {
		// Given
		String postId = "post_001";
		when(commentRepository.findAllByPostId(postId)).thenReturn(List.of());

		Map<String, Object> response = commentService.findCommentsByPost(postId);
		List<Comment> commentList = (List<Comment>)response.get("comments");
		assertEquals("댓글요청에 성공하였습니다.", response.get("message"));
		assertEquals(0, commentList.size());

		verify(commentRepository, times(1)).findAllByPostId(postId);

		System.out.println("✅ 댓글요청에 성공");

	}

	//댓글 조회 테스트 게시글에서 조회했는데 댓글이 달린 상황
	@Test
	public void 게시글_댓글_조회_테스트_댓글있음() {
		// Given
		String postId = "post_001";
		Comment comment =  Comment.builder().postId("post_001").build();

		when(commentRepository.findAllByPostId(postId)).thenReturn(List.of(comment,comment,comment));

		Map<String, Object> response = commentService.findCommentsByPost(postId);
		List<Comment> commentList = (List<Comment>)response.get("comments");
		assertEquals("댓글요청에 성공하였습니다.", response.get("message"));
		assertEquals(3, commentList.size());

		verify(commentRepository, times(1)).findAllByPostId(postId);

		System.out.println("✅ 댓글요청에 성공");

	}
}
