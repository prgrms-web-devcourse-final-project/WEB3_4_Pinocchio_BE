package sns.pinocchio.post;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.post.PostModifyRequest;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.util.List;
import java.util.Optional;

@Tag("integration")
@SpringBootTest
public class PostModifyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void 게시글_수정_테스트() {
        // 1️⃣ 먼저 user_123이 작성한 게시물 중 하나 조회
        Optional<Post> optionalPost = postRepository.findAllByTsid("user_123")
                .stream()
                .filter(p -> "active".equals(p.getStatus()))
                .findFirst();

        if (optionalPost.isEmpty()) {
            System.out.println("❌ 테스트용 게시물이 없습니다.");
            return;
        }

        Post originalPost = optionalPost.get();
// 테스트용 주석
        // 2️⃣ 수정 요청 생성
        PostModifyRequest request = PostModifyRequest.builder()
                .postId(originalPost.getId())
                .tsid("user_123")
                .content("✅ 수정된 게시물입니다. #수정됨")
                .imageUrls(List.of("https://example.com/modified_image.jpg"))
                .visibility("private") // 문자열로 줘도 내부에서 Enum으로 처리됨
                .build();

        // 3️⃣ 서비스 호출
        postService.modifyPost(request, "user_123");

        // 4️⃣ 확인
        Post updatedPost = postRepository.findById(originalPost.getId()).get();
        System.out.println("🟢 수정 완료 후 내용: " + updatedPost.getContent());
        System.out.println("🟢 공개 여부: " + updatedPost.getVisibility());
    }
}