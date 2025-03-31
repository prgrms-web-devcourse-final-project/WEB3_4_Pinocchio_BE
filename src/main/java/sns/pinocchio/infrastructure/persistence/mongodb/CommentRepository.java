package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;

public interface CommentRepository extends MongoRepository<Comment, String> {
	Optional<Comment> findByIdAndPostIdAndStatus(String id, String postId, CommentStatus status);
	List<Comment> findAllByPostIdAndStatus(String postId, CommentStatus status);
	Page<Comment> findAllByUserIdAndStatus(String authorId, Pageable pageable, CommentStatus status);
}
