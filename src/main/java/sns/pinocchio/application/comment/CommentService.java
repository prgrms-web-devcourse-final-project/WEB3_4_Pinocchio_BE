package sns.pinocchio.application.comment;

import static sns.pinocchio.application.comment.DeleteType.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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


	//댓글 생성 메서드
	public Map<String, Object> createComment(CommentCreateRequest request, String authorId, String postId) {
		Comment comment = Comment.builder()
			.userTsid(authorId)
			.postId(postId)
			.content(request.getContent())
			.parentCommentId(request.getParentCommentId())
			.likes(0)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.status(CommentStatus.ACTIVE)
			.build();
		String commentId = commentRepository.save(comment).getId();

		return Map.of("message", "댓글이 등록되었습니다.", "commentId", commentId);
	}

	//댓글 삭제 메서드 SOFT_DELETED:실제로 삭제 X 안보이게만 HARD_DELETED:실제로 삭제
	public Map<String, Object> deleteComment(CommentDeleteRequest request) {
		Comment comment = commentRepository.findByIdAndPostId(request.commentId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));

		LocalDateTime updatedAt = LocalDateTime.now();

		if (request.action == SOFT_DELETED) {
			comment.setStatus(CommentStatus.DELETE);
			comment.setUpdatedAt(updatedAt);
			commentRepository.save(comment);
		} else if (request.action == HARD_DELETED) {
			commentLikeService.deleteAllCommentlikes(request.commentId);
			commentRepository.delete(comment);
		} else {
			throw new IllegalArgumentException("잘못된 요청입니다.");
		}

		Map<String, Object> response = new HashMap<>();
		response.put("postId", request.postId);
		response.put("commentId", request.commentId);
		response.put("message", "댓글이 삭제되었습니다.");
		response.put("updatedAt", updatedAt.toString());

		if (request.action == SOFT_DELETED) {
			response.put("visibility", "deleted");
		}
		return response;
	}

	//댓글 수정 메서드
	public Map<String, Object> modifyComment(CommentModifyRequest request) {
		Comment comment = commentRepository.findByIdAndPostId(request.commentId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));
		LocalDateTime updatedAt = LocalDateTime.now();
		comment.setContent(request.content);
		comment.setUpdatedAt(updatedAt);
		commentRepository.save(comment);
		return Map.of("message", "댓글이 성공적으로 수정되었습니다.", "postId", request.postId, "commentId", request.commentId,
			"updatedAt", updatedAt.toString());
	}

	//댓글 좋아요 업데이트 메서드, 댓글_좋아요 테이블에 등록 이후 댓글 좋아요 카운트 증가 or 댓글_좋아요 테이블에 삭제 이후 댓글 좋아요 카운트 감소
	public Map<String, Object> toggleCommentLike(CommentLikeRequest request, String commentId, String authorId) {
		Comment comment = commentRepository.findByIdAndPostId(commentId, request.postId)
			.orElseThrow(() -> new NoSuchElementException("등록된 댓글을 찾을 수 없습니다."));

		Optional<String> optCommentLikeId = commentLikeService.toggleCommentLike(commentId, authorId);
		boolean isLiked = optCommentLikeId.isPresent();

		int updatedLikes = comment.getLikes() + (isLiked ? 1 : -1);
		comment.setLikes(updatedLikes);
		commentRepository.save(comment);

		Map<String, Object> response = new HashMap<>();
		response.put("message", isLiked ? "좋아요 요청에 성공했습니다." : "좋아요 취소 요청에 성공했습니다.");
		response.put("userId", loginUserId);
		response.put("liked", isLiked);
		response.put("likes", updatedLikes);

		return response;
	}

	//댓글 유효성 검사 댓글과 게시글로 검색결과가 없을시 true반환
	public boolean isInvalidComment(String commentId, String postId) {
		return commentRepository.findByIdAndPostId(commentId, postId).isEmpty();
	}
}
