package sns.pinocchio.comment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentLikeRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentLikeTest {
	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private CommentLikeRepository commentLikeRepository;

	public String 댓글_생성() {
		CommentCreateRequest createRequest = CommentCreateRequest.builder()
			.userId("user_001")
			.content("댓글이지롱")
			.parentCommentId("comment_001")
			.build();
		String commentId = commentService.createComment(createRequest, "user_001", "post_001");
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 저장되지 않았습니다."));
		assertNotNull(comment);
		System.out.println("✅ 댓글이 MongoDB에 저장되었습니다.");
		return commentId;
	}

	@Test
	public void 댓글_좋아요_테스트() {
		String commentId = 댓글_생성();
		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId("post_001").build();
		Optional<String> optCommentLikeId = commentService.modifyCommentLike(likeRequest, commentId, "user_002");
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 존재하지않습니다."));
		assertFalse(optCommentLikeId.isEmpty());
		assertEquals(1, comment.getLikes());
	}

	@Test
	public void 댓글_좋아요_취소_테스트() {
		String commentId = 댓글_생성();
		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId("post_001").build();
		commentService.modifyCommentLike(likeRequest, commentId, "user_002");
		Optional<String> optCommentLikeId = commentService.modifyCommentLike(likeRequest, commentId, "user_002");
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 존재하지않습니다."));
		assertTrue(optCommentLikeId.isEmpty());
		assertEquals(0, comment.getLikes());
	}

}
