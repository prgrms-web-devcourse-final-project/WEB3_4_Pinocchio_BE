package sns.pinocchio.application.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import sns.pinocchio.config.global.enums.CancellState;
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

    //  기존 toggleLike() 제거, 매번 좋아요 +1 누적하는 구조로 변경
    //  좋아요 누르기 (매번 새로운 PostLike 생성)
    public void like(String postId, String tsid) {
        // 1. 게시글이 존재하는지 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        // 2. 좋아요 기록을 새로 생성 (중복 허용)
        PostLike newLike = PostLike.builder()
                .postId(postId)                 // 게시글 ID
                .postTsid(post.getTsid())       // 게시글 작성자 TSID
                .tsid(tsid)                     // 좋아요 누른 사용자 TSID
                .status(CancellState.ACTIVE)    // 상태 ACTIVE 고정
                .likedAt(LocalDateTime.now())   // 최초 좋아요 시각
                .updatedAt(LocalDateTime.now()) // 현재 시각
                .build();

        postLikeRepository.save(newLike);           //  매번 저장 → 기록 계속 쌓임
        postRepository.incrementLikesCount(postId, 1); //  매번 +1
    }

    //  게시글에 대해 사용자가 좋아요를 눌렀는지 여부 확인
    public boolean hasUserLiked(String postId, String tsid) {
        return postLikeRepository.existsByPostIdAndTsid(postId, tsid);
    }

}
