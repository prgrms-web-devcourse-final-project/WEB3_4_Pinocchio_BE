package sns.pinocchio.application.comment;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.domain.comment.CommentLikeStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
	private final CommentLikeRepository commentLikeRepository;

	//댓글 좋아요 업데이트 함수 좋아요가이미 있을시 삭제 없을시 추가
	public Optional<String> toggleCommentLike(String commentId, String loginUserTsid) {
		Optional<CommentLike> optCommentLike = commentLikeRepository.findByUserTsidAndCommentId(loginUserTsid,
			commentId);
		if (optCommentLike.isEmpty()) {
			CommentLike commentLike = CommentLike.builder()
				.commentId(commentId)
				.userTsid(loginUserTsid)
				.createdAt(LocalDateTime.now())
				.status(CommentLikeStatus.ACTIVE)
				.build();
			String commentLikeId = commentLikeRepository.save(commentLike).getId();
			return Optional.ofNullable(commentLikeId);
		} else {
			CommentLike commentLike = optCommentLike.get();
			CommentLikeStatus status = commentLike.getStatus();
			boolean isActive = status == CommentLikeStatus.ACTIVE;
			commentLike.setStatus(isActive ? CommentLikeStatus.DELETE : CommentLikeStatus.ACTIVE);
			String commentLikeId = commentLikeRepository.save(commentLike).getId();
			return isActive ?  Optional.empty():Optional.ofNullable(commentLikeId);
		}
	}

	//댓글에 달린 모든 좋아요 삭제
	public void deleteAllCommentlikes(String commentId){
		commentLikeRepository.deleteByCommentId(commentId);
	}

	//유저의 좋아요 리스트 가져오기
	public Map<String, Object> findLikesByUsers(String likeTsid) {
		return Map.of("CommentLikeList",
			commentLikeRepository.findAllByUserTsidAndStatus(likeTsid, CommentLikeStatus.ACTIVE));
	}
}
