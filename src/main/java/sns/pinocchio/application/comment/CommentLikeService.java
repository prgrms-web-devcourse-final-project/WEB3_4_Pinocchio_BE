package sns.pinocchio.application.comment;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
	private final CommentLikeRepository commentLikeRepository;

	//댓글 좋아요 업데이트 함수 좋아요가이미 있을시 삭제 없을시 추가
	public Optional<String> toggleCommentLike(String commentId, String authorId) {
		Optional<CommentLike> optCommentLike = commentLikeRepository.findByUserIdAndCommentId(authorId,
			commentId);
		String commentLikeId = null;
		if (optCommentLike.isEmpty()) {
			CommentLike commentLike = CommentLike.builder()
				.commentId(commentId)
				.userId(authorId)
				.createdAt(LocalDateTime.now())
				.status(CancellState.ACTIVE)
				.build();
			commentLikeId = commentLikeRepository.save(commentLike).getId();
		} else {
			CommentLike commentLike = optCommentLike.get();
			boolean isActivity = commentLike.toggleCommentLike();
			if (isActivity) {
				commentLikeId = commentLikeRepository.save(commentLike).getId();
			} else {
				commentLikeRepository.save(commentLike);
			}
		}
		return Optional.ofNullable(commentLikeId);
	}

	//댓글에 달린 모든 좋아요 삭제
	public void deleteAllCommentlikes(String commentId) {
		commentLikeRepository.deleteByCommentId(commentId);
	}

	//유저의 좋아요 리스트 가져오기
	public Map<String, Object> findLikesByUsers(String likeId) {
		return Map.of("CommentLikeList",
			commentLikeRepository.findAllByUserIdAndStatus(likeId, CancellState.ACTIVE));
	}

	public boolean isLiked(String commentId, String authorId) {
		Optional<CommentLike> optCommentLike = commentLikeRepository.findByUserIdAndCommentIdAndStatus(authorId,
			commentId, CancellState.ACTIVE);
		return optCommentLike.isPresent();
	}
}
