package sns.pinocchio.presentation.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.post.PostLikeSearchService;
import sns.pinocchio.application.post.PostSummaryDto;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "유저 정보 조회", description = "유저 정보 관련 API")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberInfoFindController {
	private final CommentService commentService;
	private final PostLikeSearchService postLikeSearchService;

	@Operation(summary = "유저 댓글 목록", description = "유저 본인의 댓글 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@GetMapping("/{userId}/activities/comments")
	public ResponseEntity<Map<String, Object>> findFindComments(@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable String userId,
		@RequestParam(value = "page", defaultValue = "0") int page) {
		Member authorMember = userDetails.getMember();
		Map<String, Object> response = commentService.findCommentsByUser(authorMember.getTsid(), page);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "유저 게시물 좋아요 목록", description = "유저 본인의 좋아요 누른 게시물 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "좋아요 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@GetMapping("/{userId}/activities/likes")
	public ResponseEntity<Map<String, Object>> findFindLikes(@PathVariable String userId,
                                                             @RequestParam(value = "page", defaultValue = "0") int page) {

        Page<PostSummaryDto> postSummaryPage = postLikeSearchService.findLikesByUserWithContent(userId, page);

        Map<String, Object> response = new HashMap<>();
		response.put("message", "게시물 좋아요 목록 요청 성공");
        response.put("likes", postSummaryPage.getContent());
        response.put("totalPages", postSummaryPage.getTotalPages());
        response.put("totalElements", postSummaryPage.getTotalElements());
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
