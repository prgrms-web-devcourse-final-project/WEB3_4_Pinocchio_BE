package sns.pinocchio.presentation.user;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.comment.CommentService;

@Tag(name = "유저 정보 조회", description = "유저 정보 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserInfoFindController {
	private final CommentService commentService;
	@Operation(summary = "유저 댓글 목록", description = "유저 본인의 댓글 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@GetMapping("/{userId}/activities/comments")
	public ResponseEntity<Map<String, Object>> findFindComments(@PathVariable String userId,@RequestParam(value="page", defaultValue="0") int page) {
		if (false/*JWT인증*/) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "유효하지 않은 인증 정보입니다."));
		}
		String authorId = "user_001";//jwt구현시 제거
		Map<String, Object> response = commentService.findCommentsByUser(authorId,page);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
