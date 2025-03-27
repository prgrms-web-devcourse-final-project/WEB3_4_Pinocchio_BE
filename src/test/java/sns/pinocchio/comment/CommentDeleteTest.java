package sns.pinocchio.comment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentDeleteRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.DeleteType;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentDeleteTest {
	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

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

	//댓글 삭제 테스트 메서드 실제 DB에 업데이트 소프트 삭제:안보이게 설정 실제 DB에 업데이트
	@Test
	public void 댓글_소프트_삭제_테스트() {
		String commentId = 댓글_생성();
		CommentDeleteRequest deleteRequest = CommentDeleteRequest.builder()
			.postId("post_001")
			.commentId(commentId)
			.action(DeleteType.SOFT_DELETED)
			.build();

		commentService.deleteComment(deleteRequest);
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 존재하지 않았습니다."));
		assertEquals(CommentStatus.DELETE, comment.getStatus());
		System.out.println("✅ 댓글이 MongoDB에서 소프트 삭제 되었습니다.");

	}

	//댓글 삭제 테스트 메서드 실제 DB에 업데이트 하드 삭제:실제로 삭제 실제 DB에 업데이트
	@Test
	public void 댓글_하드_삭제_테스트() {
		String commentId = 댓글_생성();

		CommentDeleteRequest deleteRequest = CommentDeleteRequest.builder()
			.postId("post_001")
			.commentId(commentId)
			.action(DeleteType.HARD_DELETED)
			.build();

		commentService.deleteComment(deleteRequest);
		Optional<Comment> opt = commentRepository.findById(commentId);
		assertTrue(opt.isEmpty());
		System.out.println("✅ 댓글이 MongoDB에서 하드 삭제 되었습니다.");

	}
}
