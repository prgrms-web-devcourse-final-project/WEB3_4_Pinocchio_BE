package sns.pinocchio.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentFindByMemberServiceTest {
	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentRepository commentRepository;

	//댓글 유저로 조회해서 다가져오기
	@Test
	void 댓글_유저로_조회() {
		String authorId = "user_001";
		int page = 0;
		Comment comment = Comment.builder().build();

		Page<Comment> commentPage = new PageImpl<>(List.of(comment, comment, comment, comment));

		when(commentRepository.findAllByUserIdAndStatus(anyString(), any(Pageable.class),
			any(CancellState.class))).thenReturn(commentPage);

		Map<String, Object> response = commentService.findCommentsByUser(authorId, page);
		String message = (String)response.get("message");
		Long totalElements = (Long)response.get("totalElements");

		assertEquals("댓글요청에 성공하였습니다.", message);
		assertEquals(4, totalElements);
		verify(commentRepository, times(1)).findAllByUserIdAndStatus(anyString(), any(Pageable.class),
			any(CancellState.class));
		System.out.println("✅ 댓글 유저로 조회 성공");

	}

}
