package sns.pinocchio.application.comment;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;

	public String createComment(CommentCreateRequest request, String userId, String postId) {
		Comment comment = Comment.builder()
			.userId(userId)
			.postId(postId)
			.content(request.getContent())
			.parentCommentId(request.getParentCommentId())
			.likes(0)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.status(CommentStatus.ACTIVE)
			.build();

		Comment savedComment = commentRepository.save(comment);
		return savedComment.getId();
	}
}
