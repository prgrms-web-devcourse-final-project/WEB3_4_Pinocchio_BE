package sns.pinocchio.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.post.PostCreateRequest;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.infrastructure.persistence.mysql.HashtagRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class HashtagUsageCountTest {

    @Autowired
    private PostService postService;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Test
    public void 같은_해시태그를_재사용하면_카운트가_증가한다() {
        // 1️⃣ 먼저 하나 저장
        PostCreateRequest first = PostCreateRequest.builder()
                .content("첫 번째 글 - 해시태그 저장")
                .imageUrls(List.of("https://img.com/1.jpg"))
                .hashtags(List.of("#카운트업", "#재사용"))
                .mentions(List.of("user_aaa"))
                .visibility("public")
                .build();

        postService.createPost(first, "user_aaa");

        // 2️⃣ 기존 카운트 조회
        int initialCount = hashtagRepository.findByTag("#카운트업")
                .map(h -> h.getUsageCount())
                .orElse(0);

        // 3️⃣ 같은 해시태그로 또 저장
        PostCreateRequest second = PostCreateRequest.builder()
                .content("두 번째 글 - 같은 해시태그 사용")
                .imageUrls(List.of("https://img.com/2.jpg"))
                .hashtags(List.of("#카운트업")) // 중복된 태그
                .mentions(List.of("user_bbb"))
                .visibility("private")
                .build();

        postService.createPost(second, "user_bbb");

        // 4️⃣ 다시 카운트 조회
        int updatedCount = hashtagRepository.findByTag("#카운트업")
                .map(h -> h.getUsageCount())
                .orElse(0);

        System.out.println("🟢 최초 카운트: " + initialCount);
        System.out.println("🟢 수정 후 카운트: " + updatedCount);

        // 5️⃣ 검증
        assertThat(updatedCount).isEqualTo(initialCount + 1);
    }
}
