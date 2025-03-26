package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentLike;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {
	Optional<CommentLike> findByUserIdAndCommentId(String userId, String postId);

}
