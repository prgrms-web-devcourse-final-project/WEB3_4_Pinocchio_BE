package sns.pinocchio.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.PostLike;
import sns.pinocchio.infrastructure.persistence.mongodb.PostLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostLikeSearchService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    // 게시글의 총 좋아요 수
    public long countLikes(String postId) {
        return postLikeRepository.countByPostIdAndStatus(postId, CancellState.ACTIVE);
    }

    // 특정 사용자가 누른 좋아요 목록
    public Page<PostLike> findLikesByUser(String userTsid,int page) {
        Pageable pageable = PageRequest.of(page, 15);
        return postLikeRepository.findAllByTsidAndStatus(userTsid, CancellState.ACTIVE,pageable);
    }

    // content 요약 함수
    private String summarizeContent(String content) {
        return content.length() > 15 ? content.substring(0, 15) + "..." : content;
    }

    // 좋아요 + 게시글 내용 조회 (마이페이지 용)
    public Page<PostSummaryDto> findLikesByUserWithContent(String userTsid, int page) {
        Pageable pageable = PageRequest.of(page, 15);
        Page<PostLike> likesPage = postLikeRepository.findAllByTsidAndStatus(userTsid, CancellState.ACTIVE, pageable);

        List<String> postIds = likesPage.getContent().stream()
                .map(PostLike::getPostId)
                .toList();

        List<Post> posts = postRepository.findAllById(postIds);

        Map<String, String> postIdToContent = posts.stream()
                .collect(Collectors.toMap(Post::getId, Post::getContent));

        List<PostSummaryDto> result = likesPage.getContent().stream()
                .map(like -> {
                    String rawContent = postIdToContent.getOrDefault(like.getPostId(), "삭제된 게시글입니다");
                    return new PostSummaryDto(like.getPostId(), summarizeContent(rawContent));
                })
                .toList();

        return new PageImpl<>(result, pageable, likesPage.getTotalElements());
    }

    // 특정 게시글에 좋아요 누른 사용자 목록
    public List<PostLike> findUsersByPost(String postId) {
        return postLikeRepository.findAllByPostIdAndStatus(postId, CancellState.ACTIVE);
    }
}
