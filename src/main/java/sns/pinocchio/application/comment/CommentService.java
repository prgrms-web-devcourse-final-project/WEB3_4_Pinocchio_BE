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

	public void deleteComment(CommentDeleteRequest request, String loginUserId) {
		Comment comment = commentRepository.findByIdAndUserIdAndPostId(request.commentId, loginUserId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));

		switch (request.action) {
			case SOFT_DELETED -> {
				comment.setStatus(CommentStatus.DELETE);
				commentRepository.save(comment);
			}
			case HARD_DELETED -> {
				commentRepository.delete(comment);
			}
		}
	}

	public String modifyComment(CommentModifyRequest request,String loginUserId){
		Comment comment = commentRepository.findByIdAndUserIdAndPostId(request.commentId, loginUserId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));
		comment.setContent(request.content);
		return commentRepository.save(comment).getId();
	}
}
