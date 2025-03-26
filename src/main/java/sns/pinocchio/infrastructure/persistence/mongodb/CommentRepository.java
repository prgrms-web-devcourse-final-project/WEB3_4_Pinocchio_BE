package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.domain.comment.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {
	Optional<Comment> findByIdAndPostId(String id, String postId);

}
