package sns.pinocchio.application.comment;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.domain.comment.CommentStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final CommentLikeService commentLikeService;

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

		return commentRepository.save(comment).getId();
	}

	public void deleteComment(CommentDeleteRequest request) {
		Comment comment = commentRepository.findByIdAndPostId(request.commentId, request.postId)
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

	public String modifyComment(CommentModifyRequest request) {
		Comment comment = commentRepository.findByIdAndPostId(request.commentId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));
		comment.setContent(request.content);
		return commentRepository.save(comment).getId();
	}

	public Optional<String> modifyCommentLike(CommentLikeRequest request, String commentId, String loginUserId) {
		Optional<String> optCommentLikeId = commentLikeService.modifyCommentLike(commentId, loginUserId);
		Comment comment = commentRepository.findByIdAndPostId(commentId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));
		if(Objects.equals(comment.getUserId(), loginUserId)){
			throw new IllegalArgumentException("잘못된 요청입니다.");
		}

		int commentLikes;
		if (optCommentLikeId.isEmpty()) {
			commentLikes = comment.getLikes() - 1;
		} else {
			commentLikes = comment.getLikes() + 1;
		}
		comment.setLikes(commentLikes);
		commentRepository.save(comment);
		return optCommentLikeId;
	}
}
