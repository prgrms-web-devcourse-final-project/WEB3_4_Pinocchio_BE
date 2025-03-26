package sns.pinocchio.application.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public String createPost(PostCreateRequest request, String userId) {
        Post post = Post.builder()
                .userId(userId)
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
                .hashtags(request.getHashtags())
                .mentions(request.getMentions())
                .visibility(Visibility.valueOf(request.getVisibility().toUpperCase()))
                .likes(0)
                .commentsCount(0)
                .views(0)
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    @Transactional
    public void modifyPost(PostModifyRequest request, String loginUserId) {
        Post post = postRepository.findByIdAndUserIdAndStatus(
                request.getPostId(), loginUserId, "active"
        ).orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다."));

        post.setContent(request.getContent());
        post.setImageUrls(request.getImageUrls());
        post.setVisibility(Visibility.valueOf(request.getVisibility().toUpperCase()));  // 문자열 -> enum
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);
    }

}
