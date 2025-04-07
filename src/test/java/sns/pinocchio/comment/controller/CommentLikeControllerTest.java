package sns.pinocchio.comment.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentLikeRequest;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentLikeControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MemberRepository memberRepository;

	private ResultActions loginAndGetResponse() throws Exception {
		String loginRequestJson =
			TestFixture.createLoginRequestJson("example@naver.com", "testPassword!");

		return mockMvc.perform(
			post("/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginRequestJson));
	}

	public Member setUp() {
		Member member =
			Member.builder()
				.email("example@naver.com")
				.password(passwordEncoder.encode("testPassword!"))
				.name("testName")
				.nickname("testNickname")
				.build();
		return memberRepository.save(member);
	}

	//댓글 좋아요 테스트
	@Test
	public void 댓글_좋아요_테스트() throws Exception {
		Member member = setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String postId = "post_001";
		String commentId = "comment_001";
		CommentLikeRequest request = CommentLikeRequest.builder().postId(postId).build();
		Map<String, Object> response = Map.of("message", "좋아요 요청에 성공했습니다.", "userId", "user_001", "liked", true,
			"likes", 1);

		when(commentService.toggleCommentLike(any(CommentLikeRequest.class), anyString(), anyString())).thenReturn(
			response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(false);
		mockMvc.perform(post("/comments/like/comment_001").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("좋아요 요청에 성공했습니다."))
			.andExpect(jsonPath("$.likes").value(1))
			.andExpect(jsonPath("$.liked").value(true))
			.andDo(print());

	}

	//댓글 좋아요 실패 테스트
	@Test
	public void 댓글_좋아요_실패_테스트() throws Exception {
		Member member = setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String postId = "post_001";
		String commentId = "comment_001";

		CommentLikeRequest request = CommentLikeRequest.builder().postId(postId).build();
		Map<String, Object> response = Map.of("message", "좋아요 요청에 성공했습니다.", "userId", "user_001", "liked", true,
			"likes", 1);

		when(commentService.toggleCommentLike(any(CommentLikeRequest.class), anyString(), anyString())).thenReturn(
			response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(true);

		mockMvc.perform(post("/comments/like/comment_001").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", accessToken))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("등록된 댓글을 찾을 수 없습니다."))
			.andDo(print());

	}
}
