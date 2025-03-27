package sns.pinocchio.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.domain.post.LikeStatus;
import sns.pinocchio.domain.post.PostLike;
import sns.pinocchio.infrastructure.persistence.mongodb.PostLikeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostLikeSearchService {

    private final PostLikeRepository postLikeRepository;

    // 게시글의 총 좋아요 수
    public long countLikes(String postId) {
        return postLikeRepository.countByPostIdAndStatus(postId, LikeStatus.ACTIVE);
    }

    // 특정 사용자가 누른 좋아요 목록
    public List<PostLike> findLikesByUser(String userTsid) {
        return postLikeRepository.findAllByUserTsidAndStatus(userTsid, LikeStatus.ACTIVE);
    }

    // 특정 게시글에 좋아요 누른 사용자 목록
    public List<PostLike> findUsersByPost(String postId) {
        return postLikeRepository.findAllByPostIdAndStatus(postId, LikeStatus.ACTIVE);
    }
}
