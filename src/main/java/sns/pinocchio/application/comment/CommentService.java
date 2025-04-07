package sns.pinocchio.application.comment;

import static sns.pinocchio.application.comment.DeleteType.*;
import static sns.pinocchio.presentation.comment.exception.CommentErrorCode.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.comment.commentDto.CommentCreateRequest;
import sns.pinocchio.application.comment.commentDto.CommentDeleteRequest;
import sns.pinocchio.application.comment.commentDto.CommentLikeRequest;
import sns.pinocchio.application.comment.commentDto.CommentModifyRequest;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.comment.Comment;
import sns.pinocchio.infrastructure.persistence.mongodb.CommentRepository;
import sns.pinocchio.presentation.comment.exception.CommentErrorCode;
import sns.pinocchio.presentation.comment.exception.CommentException;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;
	private final CommentLikeService commentLikeService;

	//댓글 생성 메서드
	public Map<String, Object> createComment(CommentCreateRequest request, String authorId) {
		Comment comment = Comment.builder()
			.userId(authorId)
			.postId(request.getPostId())
			.content(request.getContent())
			.parentCommentId(request.getParentCommentId())
			.likes(0)
			.createdAt(LocalDateTime.now())
			.updatedAt(LocalDateTime.now())
			.status(CancellState.ACTIVE)
			.build();
		String commentId = commentRepository.save(comment).getId();

		return Map.of("message", "댓글이 등록되었습니다.", "commentId", commentId);
	}

	//댓글 삭제 메서드 SOFT_DELETED:실제로 삭제 X 안보이게만 HARD_DELETED:실제로 삭제
	public Map<String, Object> deleteComment(CommentDeleteRequest request) {
		Comment comment = commentRepository.findByIdAndPostIdAndStatus(request.getCommentId(), request.getPostId(),
				CancellState.ACTIVE)
			.orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND));

		LocalDateTime updatedAt = LocalDateTime.now();

		if (request.getAction() == SOFT_DELETED) {
			comment.setStatus(CancellState.CANCELLED);
			comment.setUpdatedAt(updatedAt);
			commentRepository.save(comment);
		} else if (request.getAction() == HARD_DELETED) {
			commentLikeService.deleteAllCommentlikes(request.getCommentId());
			commentRepository.delete(comment);
		} else {
			throw new CommentException(CommentErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("postId", request.getPostId());
		response.put("commentId", request.getCommentId());
		response.put("message", "댓글이 삭제되었습니다.");
		response.put("updatedAt", updatedAt.toString());

		if (request.getAction() == SOFT_DELETED) {
			response.put("visibility", "deleted");
		}
		return response;
	}

	//댓글 수정 메서드
	public Map<String, Object> modifyComment(CommentModifyRequest request) {
		Comment comment = commentRepository.findByIdAndPostIdAndStatus(request.getCommentId(), request.getPostId(),
				CancellState.ACTIVE)
			.orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND));
		LocalDateTime updatedAt = LocalDateTime.now();
		comment.setContent(request.getContent());
		comment.setUpdatedAt(updatedAt);
		commentRepository.save(comment);
		return Map.of("message", "댓글이 성공적으로 수정되었습니다.", "postId", request.getPostId(), "commentId",
			request.getCommentId(),
			"updatedAt", updatedAt.toString());
	}

	//댓글 좋아요 업데이트 메서드, 댓글_좋아요 테이블에 등록 이후 댓글 좋아요 카운트 증가 or 댓글_좋아요 테이블에 삭제 이후 댓글 좋아요 카운트 감소
	public Map<String, Object> toggleCommentLike(CommentLikeRequest request, String commentId, String authorId) {
		Comment comment = commentRepository.findByIdAndPostIdAndStatus(commentId, request.getPostId(),
				CancellState.ACTIVE)
			.orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND));

		Optional<String> optCommentLikeId = commentLikeService.toggleCommentLike(commentId, authorId);
		boolean isLiked = optCommentLikeId.isPresent();

		int updatedLikes = comment.updateLikes(isLiked);
		commentRepository.save(comment);

		Map<String, Object> response = new HashMap<>();
		if (isLiked) {
			response.put("message", "좋아요 요청에 성공했습니다.");

		} else {
			response.put("message", "좋아요 취소 요청에 성공했습니다.");
		}
		response.put("userId", authorId);
		response.put("liked", isLiked);
		response.put("likes", updatedLikes);

		return response;
	}

	//게시글로 댓글 가져오기
	public Map<String, Object> findCommentsByPost(String postId) {
		List<Comment> commentList = commentRepository.findAllByPostIdAndStatus(postId, CancellState.ACTIVE);
		return Map.of("message", "댓글요청에 성공하였습니다.", "comments", commentList);
	}

	//유저로 댓글 가져오기
	public Map<String, Object> findCommentsByUser(String authorId, int page) {
		Pageable pageable = PageRequest.of(page, 15);
		Page<Comment> pagingComment = commentRepository.findAllByUserIdAndStatus(authorId, pageable,
			CancellState.ACTIVE);
		long totalElements = pagingComment.getTotalElements();
		long totalpages = pagingComment.getTotalPages();

		return Map.of("message", "댓글요청에 성공하였습니다.", "page", page, "totalElements", totalElements, "totalPages",
			totalpages, "comments",
			pagingComment.getContent());
	}

	//댓글 유효성 검사 댓글과 게시글로 검색결과가 없을시 true반환
	public boolean isInvalidComment(String commentId, String postId) {
		return commentRepository.findByIdAndPostIdAndStatus(commentId, postId, CancellState.ACTIVE).isEmpty();
	}

	//자기 댓글인지 확인
	public boolean isMyComment(String authorId, String commentId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND));
		if (Objects.equals(comment.getUserId(), authorId)) {
			return true;
		} else {
			return false;
		}
	}

	//자기 댓글아닌거 확인
	public boolean isNotMyComment(String authorId, String commentId) {
		Comment comment = commentRepository.findById(commentId)
			.orElseThrow(() -> new CommentException(COMMENT_NOT_FOUND));
		if (Objects.equals(comment.getUserId(), authorId)) {
			return false;
		} else {
			return true;
		}
	}
}
