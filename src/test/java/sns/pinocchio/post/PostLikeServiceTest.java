package sns.pinocchio.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sns.pinocchio.application.post.PostLikeService;
import sns.pinocchio.domain.post.LikeStatus;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.PostLike;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.persistence.mongodb.PostLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        // 게시글 저장 (작성자는 user_123)
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
    void 본인_게시글에는_좋아요_불가() {
        assertThrows(IllegalArgumentException.class, () ->
                postLikeService.toggleLike(savedPost.getId(), "user_123")
        );
    }

    @Test
    void 다른사람_첫_좋아요_누르면_생성됨() {
        postLikeService.toggleLike(savedPost.getId(), "user_456");

        Optional<PostLike> result = postLikeRepository.findByPostIdAndTsid(savedPost.getId(), "user_456");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(LikeStatus.ACTIVE);
    }

    @Test
    void 기존_좋아요가_있으면_토글되어_취소됨() {
        // 먼저 좋아요를 한 번 누름
        postLikeService.toggleLike(savedPost.getId(), "user_456");

        // 두 번째 눌러서 취소
        postLikeService.toggleLike(savedPost.getId(), "user_456");

        Optional<PostLike> result = postLikeRepository.findByPostIdAndTsid(savedPost.getId(), "user_456");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(LikeStatus.CANCELLED);
    }

    @Test
    void 좋아요_다시_누르면_복구됨() {
        postLikeService.toggleLike(savedPost.getId(), "user_456"); // 누름 → active
        postLikeService.toggleLike(savedPost.getId(), "user_456"); // 취소 → cancelled
        postLikeService.toggleLike(savedPost.getId(), "user_456"); // 다시 누름 → active

        Optional<PostLike> result = postLikeRepository.findByPostIdAndTsid(savedPost.getId(), "user_456");

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(LikeStatus.ACTIVE);
    }
}
