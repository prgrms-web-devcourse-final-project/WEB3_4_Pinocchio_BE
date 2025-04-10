package sns.pinocchio.MemberFollow.controller;

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
import sns.pinocchio.application.member.MemberFollowService;
import sns.pinocchio.application.member.memberDto.MemberFollowRequest;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

import java.util.Map;

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
class MemberFollowControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CommentService commentService;

	@MockBean
	private MemberFollowService  memberFollowService;

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

	//유저 팔로우 테스트
	@Test
	void 유저_팔로우_테스트() throws Exception {
		String userId = "user_002";
		String authorNickname = "고길동";
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		MemberFollowRequest request = MemberFollowRequest.builder().followingNickname(authorNickname).build();
		Map<String, Object> response = Map.of("message", "팔로우에 성공하였습니다.", "followed", true);
		when(memberFollowService.followingUser(request,userId,member.getTsid(),member.getNickname()) ).thenReturn(response);
		mockMvc.perform(post("/user/"+userId+"/follow").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우에 성공하였습니다."))
			.andExpect(jsonPath("$.followed").value(true))
			.andDo(print());
		System.out.println("✅ 유저 팔로우 성공");
	}

	//유저 팔로우 취소 테스트
	@Test
	void 유저_팔로우_취소_테스트() throws Exception {
		String userId = "user_002";
		String authorNickname = "고길동";
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		MemberFollowRequest request = MemberFollowRequest.builder().followingNickname(authorNickname).build();
		Map<String, Object> response = Map.of("message", "팔로우 취소에 성공하였습니다.", "followed", false);
		when(memberFollowService.followingUser(request,userId,member.getTsid(),member.getNickname()) ).thenReturn(response);
		mockMvc.perform(post("/user/"+userId+"/follow").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken)
				.content(new ObjectMapper().writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로우 취소에 성공하였습니다."))
			.andExpect(jsonPath("$.followed").value(false))
			.andDo(print());
		System.out.println("✅ 유저 팔로우 성공");
	}




}
