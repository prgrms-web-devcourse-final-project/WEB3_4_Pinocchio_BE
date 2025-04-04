package sns.pinocchio.post;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.post.PostCreateRequest;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.infrastructure.persistence.mysql.HashtagRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
public class HashtagCreateTest {

    @Autowired
    private PostService postService;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Test
    public void 게시글_작성시_해시태그_자동_등록된다() {
        PostCreateRequest request = PostCreateRequest.builder()
                .content("해시태그 저장 테스트입니다.")
                .imageUrls(List.of("https://img.com/1.jpg"))
                .hashtags(List.of("#테스트", "#저장"))
                .mentions(List.of("user_123"))
                .visibility("public")
                .build();

        // 게시글 생성
        String postId = postService.createPost(request, "user_999");
        System.out.println("✅ 게시글 생성됨, postId: " + postId);

        // 해시태그 존재 확인
        assertThat(hashtagRepository.findByTag("#테스트")).isPresent();
        assertThat(hashtagRepository.findByTag("#저장")).isPresent();

        System.out.println("🟢 해시태그가 정상적으로 저장되었습니다.");
    }
}
