package sns.pinocchio.presentation.comment;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.comment.CommentCreateRequest;
import sns.pinocchio.application.comment.CommentDeleteRequest;
import sns.pinocchio.application.comment.CommentLikeRequest;
import sns.pinocchio.application.comment.CommentModifyRequest;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.PostRepository;

@Tag(name = "댓글", description = "댓글 관련 API")
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
	private final CommentService commentService;
	private final MemberRepository memberRepository;
	private final PostRepository postRepository;

		@Operation(summary = "댓글 등록", description = "댓글을 등록합니다.")
		@ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 등록 성공"),
			@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
			@ApiResponse(responseCode = "404", description = "댓글 조회 실패"),
			@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
		@PutMapping("/create")
		public ResponseEntity<Map<String, Object>> updateComment(Principal principal,
			@RequestBody CommentCreateRequest request) {
			Optional<Member> optMember = memberRepository.findByName(principal.getName());
			if(optMember.isEmpty()){
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
			}

			Optional<Post> optPost =  postRepository.findById(request.getPostId());

			if (optPost.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 게시글을 찾을 수 없습니다."));
			}

			Map<String, Object> response = commentService.createComment(request,optMember.get().getTsid());
			return ResponseEntity.status(HttpStatus.OK).body(response);
		}

	@Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "404", description = "댓글 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PutMapping("/modify")
	public ResponseEntity<Map<String, Object>> updateComment(Principal principal,
		@RequestBody CommentModifyRequest request) {
		Optional<Member> optMember = memberRepository.findByName(principal.getName());
		if(optMember.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}
		Member authorMember = optMember.get();
		if (commentService.isNotMyComment(authorMember.getTsid(), request.getCommentId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("message", "이 댓글을 수정할 권한이 없습니다. 작성자만 수정할 수 있습니다."));
		}

		if (commentService.isInvalidComment(request.getCommentId(), request.getPostId())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 댓글을 찾을 수 없습니다."));
		}

		Map<String, Object> response = commentService.modifyComment(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "댓글 좋아요", description = "댓글을 좋아요 토글")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "좋아요 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "404", description = "댓글 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{commentId}/like")
	public ResponseEntity<Map<String, Object>> toggleCommentLike(Principal principal, @PathVariable String commentId,
		@RequestBody CommentLikeRequest request) {
		Optional<Member> optMember = memberRepository.findByName(principal.getName());
		if(optMember.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Member authorMember = optMember.get();

		if (commentService.isMyComment(authorMember.getTsid(), commentId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("message", "스스로 좋아요는 불가능합니다."));
		}

		if (commentService.isInvalidComment(commentId, request.getPostId())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 댓글을 찾을 수 없습니다."));
		}

		Map<String, Object> response = commentService.toggleCommentLike(request, commentId, authorMember.getTsid());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "404", description = "댓글 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@DeleteMapping
	public ResponseEntity<Map<String, Object>> deleteComment(Principal principal,
		@RequestBody CommentDeleteRequest request) {
		Optional<Member> optMember = memberRepository.findByName(principal.getName());
		if(optMember.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Member authorMember = optMember.get();

		if (commentService.isNotMyComment(authorMember.getTsid(), request.getCommentId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("message", "권한이 없습니다."));
		}

		if (commentService.isInvalidComment(request.getCommentId(), request.getPostId())) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 댓글을 찾을 수 없습니다."));
		}
		Map<String, Object> response = commentService.deleteComment(request);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "게시글로 댓글 조회", description = "댓글을 조회합니다.")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 조회 성공"),
		@ApiResponse(responseCode = "404", description = "댓글 조회 실패")
	})
	@GetMapping("/{postId}")
	public ResponseEntity<Map<String, Object>> findComment(@PathVariable String postId) {

		Optional<Post> optPost =  postRepository.findById(postId);

		if (optPost.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 게시글을 찾을 수 없습니다."));
		}
		Map<String, Object> response = commentService.findCommentsByPost(postId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
