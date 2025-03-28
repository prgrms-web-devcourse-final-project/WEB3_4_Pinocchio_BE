package sns.pinocchio.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
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

public class CommentCreateServiceTest {
	@Autowired
	private CommentService commentServiceReal;
	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentRepository commentRepositoryMock;

	//댓글 생성 테스트 메서드 Mock으로 실행
	@Test
	void 댓글_생성_테스트() {
		String authorId = "user_001";
		String postId = "post_001";
		String commentId = "comment_001";

		CommentCreateRequest createRequest = CommentCreateRequest.builder().authorId(authorId).content("댓글이지롱").build();

		Comment mockComment = Comment.builder().id(commentId).userId(authorId).postId(postId).content("댓글이지롱").build();

		when(commentRepositoryMock.save(any(Comment.class))).thenReturn(mockComment);
		when(commentRepositoryMock.findById(commentId)).thenReturn(Optional.of(mockComment));

		Map<String, Object> response = commentService.createComment(createRequest, authorId, postId);
		String createdCommentId = (String)response.get("commentId");

		assertNotNull(createdCommentId);
		assertEquals(commentId, createdCommentId);

		verify(commentRepositoryMock, times(1)).save(any(Comment.class));
		System.out.println("✅ 댓글 생성 성공");

	}


	@Test
	void 댓글_생성_테스트_진짜() {
		String authorId = "user_001";
		String postId = "post_001";
		String commentId = "comment_001";

		CommentCreateRequest createRequest = CommentCreateRequest.builder().authorId(authorId).content("댓글이지롱").build();


		Map<String, Object> response = commentServiceReal.createComment(createRequest, authorId, postId);
		String createdCommentId = (String)response.get("commentId");

		assertNotNull(createdCommentId);
		assertEquals(commentId, createdCommentId);

		System.out.println("✅ 댓글 생성 성공");

	}

}
