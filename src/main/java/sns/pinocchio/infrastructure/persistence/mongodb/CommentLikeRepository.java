package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentLike;
import sns.pinocchio.domain.comment.CommentLikeStatus;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
	Optional<CommentLike> findByUserIdAndCommentId(String userId, String postId);
	long countByCommentIdAndStatus(String postId,CommentLikeStatus status);
	List<CommentLike> findAllByUserIdAndStatus(String userId, CommentLikeStatus status);
	void deleteByCommentId(String commentId);

}
