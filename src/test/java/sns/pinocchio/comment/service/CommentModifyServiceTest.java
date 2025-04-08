package sns.pinocchio.comment.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentModifyRequest;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;
import sns.pinocchio.presentation.comment.exception.CommentException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@Tag("unit")
@SpringBootTest
public class CommentModifyServiceTest {
	@Mock
	private CommentRepository commentRepository;

	@InjectMocks
	private CommentService commentService;

	//댓글 수정 테스트
	@Test
	public void 댓글_수정_테스트() {
		String commentId = "comment_001";
		String postId = "post_001";
		String originalContent = "원래 댓글";
		String updatedContent = "댓글수정됐지롱";
		LocalDateTime createdAt = LocalDateTime.now();
		LocalDateTime updatedAt = LocalDateTime.now();

		Comment mockComment = Comment.builder()
			.id(commentId)
			.postId(postId)
			.content(originalContent)
			.createdAt(createdAt)
			.build();

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE)).thenReturn(
			Optional.of(mockComment));

		when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
			Comment savedComment = invocation.getArgument(0);
			savedComment.setUpdatedAt(updatedAt);
			return savedComment;
		});

		CommentModifyRequest modifyRequest = CommentModifyRequest.builder()
			.commentId(commentId)
			.postId(postId)
			.content(updatedContent)
			.build();

		commentService.modifyComment(modifyRequest);

		assertEquals(updatedContent, mockComment.getContent());
		assertNotNull(mockComment.getUpdatedAt());

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE);
		verify(commentRepository, times(1)).save(mockComment);

		System.out.println("✅ 댓글이 MongoDB에서 수정되었습니다.");
	}

	//댓글 수정 실패 테스트 (없는댓글)
	@Test
	public void 댓글_수정_실패_테스트_없는댓글() {
		// Given
		String nonExistentCommentId = "comment_999";
		String postId = "post_001";
		String updatedContent = "수정할 댓글";

		when(commentRepository.findByIdAndPostIdAndStatus(nonExistentCommentId, postId,
			CancellState.ACTIVE)).thenReturn(Optional.empty());

		CommentModifyRequest modifyRequest = CommentModifyRequest.builder()
			.commentId(nonExistentCommentId)
			.postId(postId)
			.content(updatedContent)
			.build();

		CommentException thrownException = assertThrows(CommentException.class,
			() -> commentService.modifyComment(modifyRequest));

		assertEquals("등록된 댓글을 찾을 수 없습니다.", thrownException.getMessage());

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(nonExistentCommentId, postId,
			CancellState.ACTIVE);
		verify(commentRepository, times(0)).save(any());

		System.out.println("✅ 댓글 수정 실패(없는댓글) 성공");
	}
}
