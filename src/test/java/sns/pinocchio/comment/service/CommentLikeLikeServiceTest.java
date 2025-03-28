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
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.domain.comment.CommentLikeStatus;
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
		String loginUserTsid = "user_002";
		String commentLikeId = "like_001";

		when(commentLikeRepository.findByUserTsidAndCommentId(loginUserTsid, commentId)).thenReturn(Optional.empty());
		CommentLike mockLike = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userTsid(loginUserTsid)
			.createdAt(LocalDateTime.now())
			.build();

		when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(mockLike);

		Optional<String> result = commentLikeService.toggleCommentLike(commentId, loginUserTsid);

		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(commentLikeId, result.get());

		verify(commentLikeRepository, times(1)).findByUserTsidAndCommentId(loginUserTsid, commentId);
		verify(commentLikeRepository, times(1)).save(any(CommentLike.class));

		System.out.println("✅ 댓글 좋아요 추가 성공");
	}

	@Test
	void 좋아요_테이블_삭제_테스트() {
		String commentId = "comment_001";
		String loginUserTsid = "user_002";
		String commentLikeId = "like_001";

		CommentLike mockLike = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userTsid(loginUserTsid)
			.status(CommentLikeStatus.ACTIVE)
			.createdAt(LocalDateTime.now())
			.build();

		CommentLike mockLikeCancel = CommentLike.builder()
			.id(commentLikeId)
			.commentId(commentId)
			.userTsid(loginUserTsid)
			.status(CommentLikeStatus.DELETE)
			.createdAt(LocalDateTime.now())
			.build();


		when(commentLikeRepository.findByUserTsidAndCommentId(loginUserTsid, commentId)).thenReturn(
			Optional.of(mockLike ));
		when(commentLikeRepository.save(any(CommentLike.class))).thenReturn(mockLikeCancel);


		Optional<String> result = commentLikeService.toggleCommentLike(commentId, loginUserTsid);

		Assertions.assertTrue(result.isEmpty());

		verify(commentLikeRepository, times(1)).findByUserTsidAndCommentId(loginUserTsid, commentId);
		verify(commentLikeRepository, times(1)).save(mockLike);

		System.out.println("✅ 댓글 좋아요 취소 성공");
	}
}
