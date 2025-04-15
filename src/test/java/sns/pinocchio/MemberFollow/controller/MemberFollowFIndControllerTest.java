package sns.pinocchio.MemberFollow.controller;

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
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MemberFollowFIndControllerTest {
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
	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로워_조회_테스트() throws Exception {
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String followingId = member.getTsid();
		String followingNickname = member.getNickname();
		int page = 0;


		Map<String,String> info = Map.of("userId", followingId, "nickname", followingNickname);
		List<Map<String, String>> followings = List.of(info,info,info,info,info);

		Map<String, Object> response = Map.of("message", "팔로워 조회에 성공하였습니다.", "followers", followings);
		when(memberFollowService.findFollowers(followingId,page) ).thenReturn(response);

		mockMvc.perform(get("/user/"+followingId+"/followers").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로워 조회에 성공하였습니다."))
			.andExpect(jsonPath("$.followers.length()").value(5))
			.andDo(print());
		System.out.println("✅ 유저 팔로워 조회 성공");
	}

	//유저 팔로워 조회 테스트
	@Test
	void 유저_팔로잉_조회_테스트() throws Exception {
		Member member =  setUp();
		ResultActions loginResponse = loginAndGetResponse();
		String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
		String followerId = member.getTsid();
		String followerNickname = member.getNickname();
		int page = 0;

		Map<String,String> info = Map.of("userId", followerId, "nickname", followerNickname);
		List<Map<String, String>> followers = List.of(info,info,info,info,info);

		Map<String, Object> response = Map.of("message", "팔로잉 조회에 성공하였습니다.", "followings", followers);
		when(memberFollowService.findFollowings(followerId,page) ).thenReturn(response);

		mockMvc.perform(get("/user/"+followerId+"/followings").contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", accessToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("팔로잉 조회에 성공하였습니다."))
			.andExpect(jsonPath("$.followings.length()").value(5))
			.andDo(print());
		System.out.println("✅ 유저 팔로잉 조회 성공");
	}
}
