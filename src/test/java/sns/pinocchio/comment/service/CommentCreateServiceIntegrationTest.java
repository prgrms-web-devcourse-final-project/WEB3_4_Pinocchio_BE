package sns.pinocchio.comment.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentCreateRequest;

import java.util.Map;

@Tag("integration")
@SpringBootTest
class CommentCreateServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Test
    void 댓글_생성_테스트_진짜() {
        String authorId = "user_001";
        String postId = "post_001";

        for (int i = 0; i < 100; i++) {
            CommentCreateRequest createRequest = CommentCreateRequest.builder()
                    .postId(postId)
                    .content("댓글이지롱" + i)
                    .build();
            Map<String, Object> response = commentService.createComment(createRequest, authorId);
        }

        System.out.println("✅ 댓글 생성 성공 (통합 테스트)");
    }
}