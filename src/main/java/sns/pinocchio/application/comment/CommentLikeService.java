package sns.pinocchio.application.comment;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentLikeRepository;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
	private final CommentLikeRepository commentLikeRepository;

	//댓글 좋아요 업데이트 함수 좋아요가이미 있을시 삭제 없을시 추가
	public Optional<String> modifyCommentLike(String commentId,String loginUserId){
		Optional<CommentLike> optCommentLike = commentLikeRepository.findByUserIdAndCommentId(loginUserId, commentId);
		if(optCommentLike.isEmpty()){
			CommentLike commentLike = CommentLike.builder()
				.commentId(commentId)
				.userId(loginUserId)
				.createdAt(LocalDateTime.now())
				.build();
			String commentLikeId = commentLikeRepository.save(commentLike).getId();
			return Optional.ofNullable(commentLikeId);
		} else{
			CommentLike commentLike = optCommentLike.get();
			commentLikeRepository.delete(commentLike);
			return Optional.empty();
		}
	}
}
