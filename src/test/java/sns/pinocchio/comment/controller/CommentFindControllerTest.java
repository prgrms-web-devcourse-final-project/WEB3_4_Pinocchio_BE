package sns.pinocchio.comment.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import jakarta.transaction.Transactional;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentFindControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockBean
	private PostRepository postRepository;

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

	//댓글 조회 테스트 게시글에서 조회했는데 댓글이 하나도 안달린 상황
	@Test
	void 게시글_댓글_조회_테스트_댓글없음() throws Exception {
		Member member = setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String postId = "post_001";

		Map<String, Object> response = Map.of("message", "댓글요청에 성공하였습니다.", "comments", List.of());

		when(commentService.findCommentsByPost(postId)).thenReturn(response);
		when(postRepository.findByIdAndStatus(postId, "active")).thenReturn(Optional.of(Post.builder().build()));

		mockMvc.perform(get("/comments/" + postId).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글요청에 성공하였습니다."))
			.andExpect(jsonPath("$.comments").isEmpty())
			.andDo(print());
		System.out.println("✅ 댓글 삭제 성공");
	}

	//댓글 삭제 테스트 게시글에서 조회했는데 댓글이 달린상황
	@Test
	void 게시글_댓글_조회_테스트_댓글있음() throws Exception {
		Member member = setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String postId = "post_001";
		Comment comment = Comment.builder().postId("post_001").build();

		Map<String, Object> response = Map.of("message", "댓글요청에 성공하였습니다.", "comments",
			List.of(comment, comment, comment));

		when(commentService.findCommentsByPost(postId)).thenReturn(response);
		when(postRepository.findByIdAndStatus(postId, "active")).thenReturn(Optional.of(Post.builder().build()));

		mockMvc.perform(get("/comments/" + postId).contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글요청에 성공하였습니다."))
			.andExpect(jsonPath("$.comments.length()").value(3))
			.andDo(print());
		System.out.println("✅ 댓글 삭제 성공");
	}

}
