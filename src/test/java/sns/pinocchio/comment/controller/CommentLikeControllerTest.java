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
import sns.pinocchio.application.comment.commentDto.CommentLikeRequest;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
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


        when(commentService.isInvalidComment(commentId, postId)).thenReturn(false);
        when(commentService.toggleCommentLike(any(), anyString(), anyString()))
                .thenReturn(response);

		mockMvc.perform(post("/comments/comment_001/like").contentType(MediaType.APPLICATION_JSON)
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

        // 예외 발생을 명시적으로 설정
        when(commentService.isInvalidComment(commentId, postId))
                .thenReturn(true); // controller 내부에서 조건 체크 후 예외 던짐

		mockMvc.perform(post("/comments/comment_001/like").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", accessToken))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."))
			.andDo(print());

	}
}
