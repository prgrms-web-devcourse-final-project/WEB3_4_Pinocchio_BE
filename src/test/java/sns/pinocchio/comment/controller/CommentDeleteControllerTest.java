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
import sns.pinocchio.application.comment.commentDto.CommentDeleteRequest;
import sns.pinocchio.domain.fixtures.TestFixture;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentDeleteControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private CommentService commentService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private MemberRepository memberRepository;

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

  // 댓글 삭제 테스트
  @Test
  void 댓글_삭제_테스트() throws Exception {
    Member member = setUp();
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
    String commentId = "comment_001";
    String postId = "post_001";

    CommentDeleteRequest request =
        CommentDeleteRequest.builder().commentId(commentId).postId(postId).build();
    Map<String, Object> response = Map.of("message", "댓글이 삭제되었습니다.");

      when(commentService.isNotMyComment(member.getTsid(), commentId)).thenReturn(false);
      when(commentService.isInvalidComment(commentId, postId)).thenReturn(false);
      when(commentService.deleteComment(any(CommentDeleteRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            delete("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."))
        .andDo(print());
    System.out.println("✅ 댓글 삭제 성공");
  }

  // 댓글 삭제 실패 테스트 댓글없음
  @Test
  void 댓글_삭제_실패_테스트_댓글없음() throws Exception {
    Member member = setUp();
    ResultActions loginResponse = loginAndGetResponse();
    String accessToken = loginResponse.andReturn().getResponse().getHeader("Authorization");
    String commentId = "comment_001";
    String postId = "post_001";

    CommentDeleteRequest request =
        CommentDeleteRequest.builder().commentId(commentId).postId(postId).build();

      when(commentService.isNotMyComment(member.getTsid(), commentId)).thenReturn(false);
      when(commentService.isInvalidComment(commentId, postId)).thenReturn(true);

      // ❗️컨트롤러 내부 로직이 예외를 던지므로 deleteComment는 호출되지 않음


      mockMvc
        .perform(
            delete("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request))
                .header("Authorization", accessToken))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("등록된 댓글을 찾을 수 없습니다."))
        .andDo(print());
    System.out.println("✅ 댓글 삭제 실패 성공");
  }
}
