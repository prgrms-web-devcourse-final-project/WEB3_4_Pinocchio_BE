package sns.pinocchio.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sns.pinocchio.application.post.PostLikeService;
import sns.pinocchio.presentation.post.PostLikeController;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@WebMvcTest(PostLikeController.class)
public class PostLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostLikeService postLikeService;

    @Test
    @DisplayName("좋아요 토글 - 성공")
    void toggleLike_success() throws Exception {
        // given
        String postId = "66028c8f2f5d661f0cd933ef";
        String accessToken = "Bearer mockTsid"; // 현재는 mock 처리

        doNothing().when(postLikeService).toggleLike(postId, "mockTsid");

        // when & then
        mockMvc.perform(post("/api/posts/like/{postId}", postId)
                        .header("Authorization", accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("좋아요 상태가 변경되었습니다."));

        // verify
        verify(postLikeService).toggleLike(postId, "mockTsid");
    }
}
