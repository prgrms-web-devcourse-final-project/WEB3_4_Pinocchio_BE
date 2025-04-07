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
import sns.pinocchio.application.comment.commentDto.CommentModifyRequest;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentModifyControllerTest {
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

	//댓글 수정 테스트
	@Test
	public void 댓글_수정_테스트() throws Exception {
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String commetId = "comment_001";
		String postId = "post_001";

		CommentModifyRequest request = CommentModifyRequest.builder()
			.commentId(commetId)
			.postId(postId)
			.content("수정된 댓글 내용")
			.build();
		Map<String, Object> response = Map.of("message", "댓글이 성공적으로 수정되었습니다.", "postId", postId, "commentId", commetId,
			"updatedAt", LocalDateTime.now().toString());

		when(commentService.modifyComment(any(CommentModifyRequest.class))).thenReturn(response);
		mockMvc.perform(put("/comments").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글이 성공적으로 수정되었습니다."))
			.andExpect(jsonPath("$.commentId").value(commetId))
			.andDo(print());

	}

	//댓글 수정 실패 테스트 댓글없음
	@Test
	public void 댓글_수정_테스트_실패_댓글없음() throws Exception {
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String commentId = "comment_001";
		String postId = "post_001";

		CommentModifyRequest request = CommentModifyRequest.builder()
			.commentId(commentId)
			.postId(postId)
			.content("수정된 댓글 내용")
			.build();
		Map<String, Object> response = Map.of("message", "댓글이 성공적으로 수정되었습니다.", "postId", postId, "commentId", commentId,
			"updatedAt", LocalDateTime.now().toString());

		when(commentService.modifyComment(any(CommentModifyRequest.class))).thenReturn(response);
		when(commentService.isInvalidComment(commentId, postId)).thenReturn(true);

		mockMvc.perform(put("/comments").contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(request))
				.header("Authorization", accessToken))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("등록된 댓글을 찾을 수 없습니다."))
			.andDo(print());
	}
}
