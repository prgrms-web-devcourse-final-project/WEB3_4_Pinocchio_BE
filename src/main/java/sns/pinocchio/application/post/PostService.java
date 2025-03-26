package sns.pinocchio.application.post;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.domain.post.Hashtag;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;
import sns.pinocchio.infrastructure.persistence.mysql.HashtagRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final HashtagRepository hashtagRepository;

    // 게시물 생성
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

        // 📌 해시태그 MySQL 반영
        updateHashtagUsage(request.getHashtags());

        return savedPost.getId();
    }

    //  게시물 생성시 해시태그 저장
    private void updateHashtagUsage(List<String> hashtags) {
        for (String tag : hashtags) {
            hashtagRepository.findByTag(tag)
                    .ifPresentOrElse(
                            hashtag -> {
                                hashtag.setUsageCount(hashtag.getUsageCount() + 1);
                                hashtagRepository.save(hashtag);
                            },
                            () -> {
                                Hashtag newTag = Hashtag.builder()
                                        .tag(tag)
                                        .usageCount(1)
                                        .build();
                                hashtagRepository.save(newTag);
                            }
                    );
        }
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
