package sns.pinocchio.post;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;

import java.time.LocalDateTime;
import java.util.List;

@Tag("integration")
@SpringBootTest
public class PostCreateTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void 게시글_저장_테스트() {
        Post post = Post.builder()
                .tsid("user_123")
                .content("테스트 게시글입니다.")
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .hashtags(List.of("#테스트", "#몽고"))
                .likes(0)
                .commentsCount(0)
                .views(0)
                .visibility(Visibility.PUBLIC)  // Enum → String
                .mentions(List.of("user_456"))
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mongoTemplate.insert(post);

        System.out.println("✅ 게시글이 MongoDB에 저장되었습니다.");
    }

    @Test
    public void 게시글_저장_테스트2() {
        Post post = Post.builder()
                .tsid("user_456")
                .content("테스트 게시글입니다2.")
                .imageUrls(List.of("https://example.com/image1.jpg"))
                .hashtags(List.of("#테스트2", "#몽고2"))
                .likes(0)
                .commentsCount(0)
                .views(0)
                .visibility(Visibility.PRIVATE)  // Enum → String
                .mentions(List.of("user_123"))
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mongoTemplate.insert(post);

        System.out.println("✅ 게시글이 MongoDB에 저장되었습니다.");
    }
}