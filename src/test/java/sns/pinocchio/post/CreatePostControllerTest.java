package sns.pinocchio.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sns.pinocchio.application.post.PostCreateRequest;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.presentation.post.PostController;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class CreatePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/posts - 게시글 생성 성공")
    void createPost_Success() throws Exception {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .content("테스트 게시물입니다.")
                .imageUrls(List.of("https://img.com/image1.jpg"))
                .hashtags(List.of("#테스트", "#스웨거"))
                .mentions(List.of("user_123"))
                .visibility("public")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        when(postService.createPost(request, "mockUser"))
                .thenReturn("post_12345");

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer dummyToken")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("post_12345"));
    }
}
