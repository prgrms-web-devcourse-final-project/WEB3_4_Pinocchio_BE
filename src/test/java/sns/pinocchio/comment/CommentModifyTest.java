package sns.pinocchio.comment;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentModifyRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentModifyTest {
	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

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

	@Test
	public void 댓글_수정_테스트() {
		String createdCommentId = 댓글_생성();
		CommentModifyRequest modifyRequest = CommentModifyRequest.builder()
			.commentId(createdCommentId)
			.postId("post_001")
			.content("댓글수정됐지롱")
			.build();
		commentService.modifyComment(modifyRequest);
		Comment comment = commentRepository.findById(createdCommentId)
			.orElseThrow(() -> new NoSuchElementException("댓글이 존재하지 않습니다."));
		assertEquals("댓글수정됐지롱", comment.getContent());
		System.out.println("✅ 댓글이 MongoDB에서 수정되었습니다.");

	}
}
