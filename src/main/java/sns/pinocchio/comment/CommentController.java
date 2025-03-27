package sns.pinocchio.comment;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.comment.CommentDeleteRequest;
import sns.pinocchio.application.comment.CommentLikeRequest;
import sns.pinocchio.application.comment.CommentModifyRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.DeleteType;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;

	//댓글 수정하기위한 메서드 인증정보와 poserId,commentId,userId,conent정보가 들어있는 CommentModifyDto가 필요
	@PutMapping("/modify")
	public ResponseEntity<Map<String, Object>> updateComment(Principal principal,
		@RequestBody CommentModifyRequest request) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "유효하지 않은 인증 정보입니다."));
		}
		if (commentService.isInvalidComment(request.getCommentId(), request.getPostId())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 댓글을 찾을 수 없습니다."));
		}
		if (true/*사용자와 댓글 작성자 확인 + 관리자권한 프리패스*/) {

		}
		Map<String, Object> response = commentService.modifyComment(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("/{commentId}/like")
	public ResponseEntity<Map<String, Object>> setCommentLike(Principal principal, @PathVariable String commentId,
		@RequestBody CommentLikeRequest request) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "유효하지 않은 인증 정보입니다."));
		}
		if (true/*본인 댓글인지 확인해서 400에러 발생 자기 댓글 좋아요는 불가능*/) {

		}
		if (commentService.isInvalidComment(commentId, request.getPostId())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 댓글을 찾을 수 없습니다."));
		}
		String loginUserId = principal.getName();//유저이름을 id로 쓰기 나중에 실제 유저로 바꿀것 테스트용
		Map<String, Object> response = commentService.toggleCommentLike(request, commentId, loginUserId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@DeleteMapping
	public ResponseEntity<Map<String, Object>> deleteComment(Principal principal, @RequestBody CommentDeleteRequest request) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "유효하지 않은 인증 정보입니다."));
		}
		if (true/*본인 댓글인지 확인해서 400에러 발생 자기 댓글 좋아요는 불가능*/) {

		}
		if (commentService.isInvalidComment(request.getCommentId(), request.getPostId())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 댓글을 찾을 수 없습니다."));
		}
		Map<String, Object> response = commentService.deleteComment(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
