package sns.pinocchio.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentCreateTest {
	@Autowired
	private CommentService commentService;

	@Autowired
	private CommentRepository commentRepository;

	//댓글 생성 테스트 메서드 실제 DB에 업데이트
	@Test
	void 댓글_생성_테스트() {
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
	}

}
