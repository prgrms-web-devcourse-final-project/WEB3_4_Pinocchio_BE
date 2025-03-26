package sns.pinocchio.comment;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

import org.junit.jupiter.api.Test;

@SpringBootTest
public class CommentCreateTest {
	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	@Test
	public void 댓글_저장_테스트() {
		CommentCreateRequest request = CommentCreateRequest
			.builder()
			.userId("1")
			.content("댓글이지롱")
			.parentCommentId("-1")
			.build();
		String commentId = commentService.createComment(request,"1","1");
		Comment updatedPost = commentRepository.findById(commentId ).get();
		System.out.println("✅ 댓글이 MongoDB에 저장되었습니다.");
	}
}
