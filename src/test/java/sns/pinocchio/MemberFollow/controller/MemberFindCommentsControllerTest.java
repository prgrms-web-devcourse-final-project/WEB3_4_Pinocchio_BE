package sns.pinocchio.MemberFollow.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.transaction.Transactional;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.config.global.auth.jwt.MemberAuthFilter;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.config.global.auth.service.CustomUserDetailService;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.member.MemberInfoFindController;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MemberFindCommentsControllerTest {
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
	//유저 댓글 목록 조회
	@Test
	void 유저_댓글_목록_조회() throws Exception {
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");

		Map<String,String> info = Map.of("id", "1");
		List<Map<String, String>> comments = List.of(info,info,info,info,info);
		Map<String, Object> response = Map.of("message","댓글요청에 성공하였습니다.","comments", comments);
		when(commentService.findCommentsByUser(anyString(),anyInt())).thenReturn(response);
		mockMvc.perform(get("/members/"+member.getTsid()+"/activities/comments").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("댓글요청에 성공하였습니다."))
			.andExpect(jsonPath("$.comments.length()").value(5))
			.andDo(print());
		System.out.println("✅ 유저 댓글 조회 성공");
	}


}
