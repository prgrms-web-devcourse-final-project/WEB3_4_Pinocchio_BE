package sns.pinocchio.comment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
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

	//댓글 생성 메서드 실제 DB에 업데이트
	public String 댓글_생성() {
		CommentCreateRequest createRequest = CommentCreateRequest.builder()
			.userId("user_001")
			.content("댓글이지롱")
			.build();
		Map<String, Object> response = commentService.createComment(createRequest, "user_001", "post_001");
		String commentId = (String)response.get("commentId");
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 저장되지 않았습니다."));
		assertNotNull(comment);
		System.out.println("✅ 댓글이 MongoDB에 저장되었습니다.");
		return commentId;
	}

	//댓글 좋아요 테스트 메서드, 댓글_좋아요 테이블에 등록 이후 댓글 좋아요 카운트 증가 실제 DB에 업데이트
	@Test
	public void 댓글_좋아요_테스트() {
		String commentId = 댓글_생성();
		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId("post_001").build();
		Map<String, Object> response = commentService.toggleCommentLike(likeRequest, commentId, "user_002");
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 존재하지않습니다."));
		String message = (String)response.get("message");
		assertEquals("좋아요 요청에 성공했습니다.",message);
		assertEquals(1, comment.getLikes());
	}

	//댓글 좋아요 취소 테스트 메서드, 댓글_좋아요 테이블에 등록 이후 삭제 좋아요 카운트 증가후 감소로 0 실제 DB에 업데이트
	@Test
	public void 댓글_좋아요_취소_테스트() {
		String commentId = 댓글_생성();
		CommentLikeRequest likeRequest = CommentLikeRequest.builder().postId("post_001").build();
		commentService.toggleCommentLike(likeRequest, commentId, "user_002");
		Map<String, Object> response = commentService.toggleCommentLike(likeRequest, commentId, "user_002");
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 존재하지않습니다."));
		String message = (String)response.get("message");
		assertEquals("좋아요 취소 요청에 성공했습니다.",message);
		assertEquals(0, comment.getLikes());
	}

}
