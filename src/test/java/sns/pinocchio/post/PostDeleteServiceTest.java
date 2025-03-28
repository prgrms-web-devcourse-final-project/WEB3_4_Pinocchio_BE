package sns.pinocchio.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PostDeleteServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    private Post savedPost;

    @BeforeEach
    void setup() {
        postRepository.deleteAll();

        Post post = Post.builder()
                .userTsid("user_123")
                .content("삭제 테스트용 게시글")
                .likes(0)
                .commentsCount(0)
                .views(0)
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        savedPost = postRepository.save(post);
    }

    @Test
    void 게시글_삭제_정상동작() {
        // when
        postService.deletePost(savedPost.getId(), "user_123");

        // then
        Optional<Post> result = postRepository.findById(savedPost.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo("deleted");
    }

    @Test
    void 작성자가_아닌_사용자가_삭제하면_예외() {
        assertThrows(IllegalArgumentException.class, () ->
                postService.deletePost(savedPost.getId(), "user_999")
        );
    }

    @Test
    void 이미_삭제된_게시글은_다시_삭제_불가() {
        // 선삭제
        postService.deletePost(savedPost.getId(), "user_123");

        // 다시 삭제 시도
        assertThrows(IllegalArgumentException.class, () ->
                postService.deletePost(savedPost.getId(), "user_123")
        );
    }
}
