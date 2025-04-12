package sns.pinocchio.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.post.PostLikeService;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.PostLike;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.persistence.mongodb.PostLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
public class PostLikeServiceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostLikeService postLikeService;

    private Post savedPost;

    @BeforeEach
    void setup() {
        postLikeRepository.deleteAll();
        postRepository.deleteAll();

        Post post = Post.builder()
                .tsid("user_123")
                .content("좋아요 테스트용 게시글")
                .likes(0)
                .commentsCount(0)
                .views(0)
                .visibility(Visibility.PUBLIC)
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        savedPost = postRepository.save(post);
    }

    @Test
    void 좋아요_누르면_기록이_생성된다() {
        postLikeService.like(savedPost.getId(), "user_456");

        List<PostLike> likes = postLikeRepository.findAll();

        assertThat(likes).hasSize(1);
        assertThat(likes.get(0).getPostId()).isEqualTo(savedPost.getId());
        assertThat(likes.get(0).getTsid()).isEqualTo("user_456");
    }

    @Test
    void 여러번_누르면_좋아요가_누적된다() {
        postLikeService.like(savedPost.getId(), "user_456");
        postLikeService.like(savedPost.getId(), "user_456");
        postLikeService.like(savedPost.getId(), "user_456");

        List<PostLike> likes = postLikeRepository.findAll();

        assertThat(likes).hasSize(3); // ✅ 3개 기록이 쌓여야 함
    }
}
