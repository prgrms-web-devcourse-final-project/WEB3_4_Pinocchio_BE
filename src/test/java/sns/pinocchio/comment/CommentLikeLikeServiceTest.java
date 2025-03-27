package sns.pinocchio.comment;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentLikeService;
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;

@SpringBootTest
public class CommentLikelikeServiceTest {
	@InjectMocks
	private CommentLikeService commentLikeService;

	@Mock
	private CommentLikeRepository commentLikeRepository;

	@Test
	void 좋아요_테이블_추가_테스트() {
		// Given
		String commentId = "comment_001";
		String loginUserId = "user_002";
		String commentLikeId = "like_001";

		when(commentLikeRepository.findByUserIdAndCommentId(loginUserId, commentId)).thenReturn(Optional.empty());
		CommentLike mockLike = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userId(loginUserId)
			.createdAt(LocalDateTime.now())
			.build();

		when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(mockLike);

		Optional<String> result = commentLikeService.toggleCommentLike(commentId, loginUserId);

		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(commentLikeId, result.get());

		verify(commentLikeRepository, times(1)).findByUserIdAndCommentId(loginUserId, commentId);
		verify(commentLikeRepository, times(1)).save(any(CommentLike.class));
		verify(commentLikeRepository, never()).delete(any(CommentLike.class));

		System.out.println("✅ 댓글 좋아요 추가 성공");
	}

	@Test
	void 좋아요_테이블_삭제_테스트() {
		String commentId = "comment_001";
		String loginUserId = "user_002";
		String commentLikeId = "like_001";

		CommentLike existingLike = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userId(loginUserId)
			.createdAt(LocalDateTime.now())
			.build();

		when(commentLikeRepository.findByUserIdAndCommentId(loginUserId, commentId)).thenReturn(
			Optional.of(existingLike));

		Optional<String> result = commentLikeService.toggleCommentLike(commentId, loginUserId);

		Assertions.assertTrue(result.isEmpty());

		verify(commentLikeRepository, times(1)).findByUserIdAndCommentId(loginUserId, commentId);
		verify(commentLikeRepository, times(1)).delete(existingLike);
		verify(commentLikeRepository, never()).save(any(CommentLike.class));

		System.out.println("✅ 댓글 좋아요 취소 성공");
	}
}
