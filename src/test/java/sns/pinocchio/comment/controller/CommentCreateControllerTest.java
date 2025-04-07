package sns.pinocchio.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
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
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentCreateRequest;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentCreateControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	@MockBean
	private PostRepository postRepository;

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

	//댓글 생성 테스트
	@Test
	void 댓글_생성_테스트() throws Exception {
		Member member = setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String commentId = "comment_001";
		String postId = "post_001";
		String content = "댓글";
		CommentCreateRequest request = CommentCreateRequest.builder().postId(postId).content(content).build();

		Map<String, Object> response = Map.of("message", "댓글이 등록되었습니다.", "commentId", commentId);
		when(postRepository.findByIdAndStatus(request.getPostId(), "active")).thenReturn(
			Optional.of(Post.builder().build()));
		when(commentService.createComment(request, member.getTsid())).thenReturn(response);

		mockMvc.perform(post("/comments").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글이 등록되었습니다."))
			.andDo(print());
		System.out.println("✅ 댓글 생성 성공");
	}
}
