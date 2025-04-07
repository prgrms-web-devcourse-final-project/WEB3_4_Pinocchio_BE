package sns.pinocchio.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentLikeService;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.DeleteType;
import sns.pinocchio.application.comment.commentDto.CommentDeleteRequest;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;
import sns.pinocchio.presentation.comment.exception.CommentException;

@SpringBootTest
public class CommentDeleteServiceTest {
	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private CommentLikeService commentLikeService;

	@Test
	public void 댓글_소프트_삭제_테스트() {
		// Given
		String commentId = "comment_001";
		String postId = "post_001";

		Comment comment = Comment.builder().id(commentId).postId(postId).status(CancellState.ACTIVE).build();

		CommentDeleteRequest deleteRequest = CommentDeleteRequest.builder()
			.postId(postId)
			.commentId(commentId)
			.action(DeleteType.SOFT_DELETED)
			.build();

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE)).thenReturn(
			Optional.of(comment));

		Map<String, Object> response = commentService.deleteComment(deleteRequest);

		assertEquals("댓글이 삭제되었습니다.", response.get("message"));
		assertEquals(CancellState.CANCELLED, comment.getStatus());

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE);
		verify(commentRepository, times(1)).save(comment);
		verify(commentRepository, never()).delete(any(Comment.class));

		System.out.println("✅ 댓글 소프트 삭제 성공");
	}

	// ✅ 댓글 하드 삭제 테스트
	@Test
	public void 댓글_하드_삭제_테스트() {
		// Given
		String commentId = "comment_001";
		String postId = "post_001";

		Comment comment = Comment.builder().id(commentId).postId(postId).status(CancellState.ACTIVE).build();

		CommentDeleteRequest deleteRequest = CommentDeleteRequest.builder()
			.postId(postId)
			.commentId(commentId)
			.action(DeleteType.HARD_DELETED)
			.build();

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE)).thenReturn(
			Optional.of(comment));
		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE)).thenReturn(
			Optional.of(comment));

		Map<String, Object> response = commentService.deleteComment(deleteRequest);

		assertEquals("댓글이 삭제되었습니다.", response.get("message"));

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE);
		verify(commentRepository, times(1)).delete(comment);

		System.out.println("✅ 댓글 하드 삭제 성공");

	}

	//삭제 실패 댓글이 존재하지않을때
	@Test
	public void 댓글_삭제_실패_없는댓글() {
		// Given
		String commentId = "실패Id";
		String postId = "post_001";
		CommentDeleteRequest deleteRequest = CommentDeleteRequest.builder()
			.postId(postId)
			.commentId(commentId)
			.action(DeleteType.HARD_DELETED)
			.build();

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE)).thenReturn(
			Optional.empty());

		// When & Then
		assertThrows(CommentException.class, () -> {
			commentService.deleteComment(deleteRequest);
		});

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE);
		verify(commentRepository, never()).delete(any(Comment.class));
		System.out.println("✅ 댓글 삭제 실패 (없는 댓글) 성공");

	}

	//소프트 삭제 실패 액션값 잘못들어갔을때
	@Test
	public void 댓글_삭제_실패_잘못된액션값() {
		// Given
		String commentId = "실패Id";
		String postId = "post_001";
		CommentDeleteRequest deleteRequest = CommentDeleteRequest.builder().postId(postId).commentId(commentId).build();

		Comment comment = Comment.builder().id(commentId).postId(postId).status(CancellState.ACTIVE).build();

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE)).thenReturn(
			Optional.of(comment));
		// When & Then
		assertThrows(CommentException.class, () -> {
			commentService.deleteComment(deleteRequest);
		});

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE);
		verify(commentRepository, never()).save(any(Comment.class));
		System.out.println("✅ 댓글 삭제 실패(잘못된 액션값) 성공");

	}

}
