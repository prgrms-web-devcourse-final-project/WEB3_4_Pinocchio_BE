package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.Comment;

public interface CommentRepository extends MongoRepository<Comment, String> {
	Optional<Comment> findByIdAndPostIdAndStatus(String id, String postId, CancellState status);
	List<Comment> findAllByPostIdAndStatus(String postId, CancellState status);
	Page<Comment> findAllByUserIdAndStatus(String authorId, Pageable pageable, CancellState status);
}
