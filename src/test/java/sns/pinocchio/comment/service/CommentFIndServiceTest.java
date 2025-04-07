package sns.pinocchio.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@SpringBootTest
public class CommentFIndServiceTest {
	@InjectMocks
	private CommentService commentService;

	@Mock
	private CommentRepository commentRepository;
	
	//댓글 조회 테스트 게시글에서 조회했는데 댓글이 하나도 안달린 상황
	@Test
	public void 게시글_댓글_조회_테스트_댓글없음() {
		// Given
		String postId = "post_001";
		when(commentRepository.findAllByPostIdAndStatus(postId, CancellState.ACTIVE)).thenReturn(List.of());

		Map<String, Object> response = commentService.findCommentsByPost(postId);
		List<Comment> commentList = (List<Comment>)response.get("comments");
		assertEquals("댓글요청에 성공하였습니다.", response.get("message"));
		assertEquals(0, commentList.size());

		verify(commentRepository, times(1)).findAllByPostIdAndStatus(postId, CancellState.ACTIVE);

		System.out.println("✅ 댓글 댓글 요청(댓글없음) 성공");

	}

	//댓글 조회 테스트 게시글에서 조회했는데 댓글이 달린 상황
	@Test
	public void 게시글_댓글_조회_테스트_댓글있음() {
		// Given
		String postId = "post_001";
		Comment comment = Comment.builder().postId("post_001").build();

		when(commentRepository.findAllByPostIdAndStatus(postId, CancellState.ACTIVE)).thenReturn(
			List.of(comment, comment, comment));

		Map<String, Object> response = commentService.findCommentsByPost(postId);
		List<Comment> commentList = (List<Comment>)response.get("comments");
		assertEquals("댓글요청에 성공하였습니다.", response.get("message"));
		assertEquals(3, commentList.size());

		verify(commentRepository, times(1)).findAllByPostIdAndStatus(postId, CancellState.ACTIVE);

		System.out.println("✅ 게시글 댓글 조회 성공");

	}

	//유저 댓글 목록 조회 성공
	@Test
	void 유저_댓글_목록_조회_테스트() {
		String authorId = "user_001";
		String postId = "post_001";
		String content = "댓글";
		int page = 0;
		Pageable pageable = PageRequest.of(page, 15);

		Comment comment = Comment.builder().userId(authorId).postId(postId).content(content).build();

		Page<Comment> followingsPage = new PageImpl<>(List.of(comment, comment, comment));

		when(commentRepository.findAllByUserIdAndStatus(authorId, pageable, CancellState.ACTIVE)).thenReturn(
			followingsPage);

		Map<String, Object> result = commentService.findCommentsByUser(authorId, page);
		String meesage = (String)result.get("message");

		List<Map<String, String>> comments = (List<Map<String, String>>)result.get("comments");

		assertEquals("댓글요청에 성공하였습니다.", meesage);
		assertEquals(3, comments.size());
		verify(commentRepository, times(1)).findAllByUserIdAndStatus(authorId, pageable, CancellState.ACTIVE);
		System.out.println("✅ 유저 댓글 목록 조회 성공");

	}
}
