package sns.pinocchio.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;

    private String publicPostId;
    private String privatePostId;
    private String nickname;

    @BeforeEach
    void setUp() {
        // 고유한 식별자 생성
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String tsid = "tsid_" + uniqueId;
        nickname = "닉네임_" + uniqueId;

        // 회원 저장
        Member member = Member.builder()
                .email("user+" + uniqueId + "@test.com")
                .name("홍길동")
                .nickname(nickname)
                .password("encoded")
                .build();

        ReflectionTestUtils.setField(member, "tsid", tsid);
        memberRepository.save(member);

        // 공개 게시글 저장
        Post publicPost = Post.builder()
                .tsid(tsid)
                .content("공개글입니다")
                .imageUrls(List.of("https://cdn.example.com/image.jpg"))
                .hashtags(List.of("#여행", "#제주도"))
                .likes(10)
                .commentsCount(5)
                .views(0)
                .visibility(Visibility.PUBLIC)
                .mentions(List.of("tsid_456"))
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 비공개 게시글 저장
        Post privatePost = Post.builder()
                .tsid(tsid)
                .content("비공개글입니다")
                .imageUrls(List.of())
                .hashtags(List.of())
                .likes(0)
                .commentsCount(0)
                .views(0)
                .visibility(Visibility.PRIVATE)
                .mentions(List.of())
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        publicPostId = postRepository.save(publicPost).getId();
        privatePostId = postRepository.save(privatePost).getId();
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 - 공개 게시글")
    void getPublicPostDetail() throws Exception {
        mockMvc.perform(get("/api/posts/" + publicPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(publicPostId))
                .andExpect(jsonPath("$.nickname").value(nickname)) // 동적으로 비교
                .andExpect(jsonPath("$.visibility").value("PUBLIC")); // enum은 그대로 노출됨
    }

    // 실패하는게 당연한 테스트 실패가 성공임
    @Test
    @DisplayName("게시글 상세 조회 실패 - 비공개 게시글, 인증 없음")
    void getPrivatePostWithoutAuthentication() throws Exception {
        // when & then
        mockMvc.perform(get("/api/posts/" + privatePostId))
                .andExpect(status().isForbidden())
                .andExpect(content().string("해당 게시물은 비공개 상태입니다."));
    }
}
