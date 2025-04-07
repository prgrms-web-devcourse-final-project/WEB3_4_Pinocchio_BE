package sns.pinocchio.comment.service;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentLikeService;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;

@SpringBootTest
public class CommentLikeLikeServiceTest {
	@InjectMocks
	private CommentLikeService commentLikeService;

	@Mock
	private CommentLikeRepository commentLikeRepository;

	@Test
	void 좋아요_테이블_추가_테스트() {
		// Given
		String commentId = "comment_001";
		String authorId = "user_002";
		String commentLikeId = "like_001";

		when(commentLikeRepository.findByUserIdAndCommentId(authorId, commentId)).thenReturn(Optional.empty());
		CommentLike mockLike = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userId(authorId)
			.createdAt(LocalDateTime.now())
			.build();

		when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(mockLike);

		Optional<String> result = commentLikeService.toggleCommentLike(commentId, authorId);

		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(commentLikeId, result.get());

		verify(commentLikeRepository, times(1)).findByUserIdAndCommentId(authorId, commentId);
		verify(commentLikeRepository, times(1)).save(any(CommentLike.class));

		System.out.println("✅ 댓글 좋아요 추가 성공");
	}

	@Test
	void 좋아요_테이블_삭제_테스트() {
		String commentId = "comment_001";
		String authorId = "user_002";
		String commentLikeId = "like_001";

		CommentLike mockLike = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userId(authorId)
			.status(CancellState.ACTIVE)
			.createdAt(LocalDateTime.now())
			.build();

		CommentLike mockLikeCancel = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userId(authorId)
			.status(CancellState.CANCELLED)
			.createdAt(LocalDateTime.now())
			.build();


		when(commentLikeRepository.findByUserIdAndCommentId(authorId, commentId)).thenReturn(
			Optional.of(mockLike ));
		when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(mockLikeCancel);


		Optional<String> result = commentLikeService.toggleCommentLike(commentId, authorId);

		Assertions.assertTrue(result.isEmpty());

		verify(commentLikeRepository, times(1)).findByUserIdAndCommentId(authorId, commentId);
		verify(commentLikeRepository, times(1)).save(mockLike);

		System.out.println("✅ 댓글 좋아요 취소 성공");
	}
}
