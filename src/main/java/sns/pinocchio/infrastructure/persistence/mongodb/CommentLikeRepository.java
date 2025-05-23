package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.CommentLike;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
	Optional<CommentLike> findByUserIdAndCommentId(String userId, String commentId);
	List<CommentLike> findAllByUserIdAndStatus(String userId, CancellState status);
	Optional<CommentLike> findByUserIdAndCommentIdAndStatus(String userId, String commentId,CancellState status);

	void deleteByCommentId(String commentId);
}
