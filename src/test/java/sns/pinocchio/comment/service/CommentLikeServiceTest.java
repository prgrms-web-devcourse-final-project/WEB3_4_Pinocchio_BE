package sns.pinocchio.comment.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.comment.CommentLikeService;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentLikeRequest;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@SpringBootTest
public class CommentLikeServiceTest {
	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private CommentLikeRepository commentLikeRepository;

	@Mock
	private CommentLikeService commentLikeService;

	//댓글 좋아요 테스트 메서드, 댓글_좋아요 테이블에 등록 이후 댓글 좋아요 카운트 증가 실제 DB에 업데이트
	@Test
	public void 댓글_좋아요_테스트() {
		String loginUserId = "user_002";
		String userId = "user_001";
		String postId = "post_001";
		String commentId = "comment_001";
		String commentLikeId = "commentLike_001";

		Comment mockComment = Comment.builder()
			.id(commentId)
			.userId(userId)
			.postId(postId)
			.content("댓글이지롱")
			.status(CommentStatus.ACTIVE)
			.likes(0)
			.build();

		when(commentLikeService.toggleCommentLike(commentId, loginUserId)).thenReturn(
			Optional.of(commentLikeId)); // 좋아요 추가됨

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId,CommentStatus.ACTIVE)).thenReturn(Optional.of(mockComment));

		when(commentRepository.save(any())).thenAnswer(invocation -> {
			Comment savedComment = invocation.getArgument(0);
			savedComment.setLikes(1); // 좋아요 수 증가 반영
			return savedComment;
		});

		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId(postId).build();
		Map<String, Object> response = commentService.toggleCommentLike(likeRequest, commentId, loginUserId);

		assertNotNull(response);
		assertEquals("좋아요 요청에 성공했습니다.", response.get("message"));
		assertEquals(1, response.get("likes"));

		verify(commentRepository, times(1)).save(any());
		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId,CommentStatus.ACTIVE);

		System.out.println("✅ 댓글 좋아요 테스트 성공");
	}

	@Test
	public void 댓글_좋아요_취소_테스트() {
		String loginUserId = "user_002";
		String userId = "user_001";
		String postId = "post_001";
		String commentId = "comment_001";

		Comment mockComment = Comment.builder()
			.id(commentId)
			.userId(userId)
			.postId(postId)
			.content("댓글이지롱")
			.status(CommentStatus.ACTIVE)
			.likes(1)
			.build();

		when(commentLikeService.toggleCommentLike(commentId, loginUserId)).thenReturn(Optional.empty());

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId,CommentStatus.ACTIVE)).thenReturn(Optional.of(mockComment));

		when(commentRepository.save(any())).thenAnswer(invocation -> {
			Comment savedComment = invocation.getArgument(0);
			savedComment.setLikes(0);
			return savedComment;
		});

		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId(postId).build();
		Map<String, Object> response = commentService.toggleCommentLike(likeRequest, commentId, loginUserId);

		assertNotNull(response);
		assertEquals("좋아요 취소 요청에 성공했습니다.", response.get("message"));
		assertEquals(0, response.get("likes"));

		verify(commentRepository, times(1)).save(any());
		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId,CommentStatus.ACTIVE);

		System.out.println("✅ 댓글 좋아요 취소 성공");
	}

	//좋아요 실패 댓글이 존재하지않을때
	@Test
	public void 좋아요_실패_없는댓글() {
		// Given
		String loginUserId = "user_002";
		String postId = "post_001";
		String commentId = "comment_001";
		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId(postId).build();

		when(commentRepository.findByIdAndPostIdAndStatus(commentId, postId,CommentStatus.ACTIVE)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(NoSuchElementException.class, () -> {
			commentService.toggleCommentLike(likeRequest, commentId, loginUserId);
		});

		verify(commentRepository, times(1)).findByIdAndPostIdAndStatus(commentId, postId,CommentStatus.ACTIVE);

		System.out.println("✅ 댓글 좋아요 실패 (없는댓글)");
	}

}
