package sns.pinocchio.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.domain.post.LikeStatus;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.PostLike;
import sns.pinocchio.infrastructure.persistence.mongodb.PostLikeRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    // 좋아요 토글 메서드 (없으면 생성, 있으면 상태 변경)
    public void toggleLike(String postId, String userTsid) {
        // 1. 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 본인의 게시글인지 확인하여 좋아요 금지
        if (post.getUserTsid().equals(userTsid)) {
            throw new IllegalArgumentException("자신의 게시글에는 좋아요를 누를 수 없습니다.");
        }

        // 3. 기존에 좋아요를 누른 기록이 있는지 확인
        PostLike postLike = postLikeRepository
                .findByPostIdAndUserTsid(postId, userTsid)
                .orElse(null);

        if (postLike == null) {
            // 4. 좋아요 기록이 없다면 새로 생성
            PostLike newLike = PostLike.builder()
                    .postId(postId)                        // 게시글 ID
                    .postTsid(post.getUserTsid())          // 게시글 작성자 TSID (자기 글인지 확인용)
                    .userTsid(userTsid)                    // 좋아요 누른 사용자 TSID
                    .status(LikeStatus.ACTIVE)             // 상태: ACTIVE로 설정
                    .likedAt(LocalDateTime.now())          // 최초 좋아요 누른 시각
                    .updatedAt(LocalDateTime.now())        // 최근 변경 시각
                    .build();

            postLikeRepository.save(newLike);              // MongoDB에 저장
        } else {
            // 5. 기존 기록이 있다면 상태 토글
            if (postLike.getStatus() == LikeStatus.ACTIVE) {
                postLike.setStatus(LikeStatus.CANCELLED);  // 취소
            } else {
                postLike.setStatus(LikeStatus.ACTIVE);     // 다시 좋아요
            }
            postLike.setUpdatedAt(LocalDateTime.now());    // 변경 시각 업데이트

            postLikeRepository.save(postLike);             // 변경 내용 저장
        }
    }
}
