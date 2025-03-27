package sns.pinocchio.presentation.user;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.user.UserFollowService;
import sns.pinocchio.infrastructure.persistence.mongodb.UserFollowRepository;

@Tag(name = "유저 팔로우", description = "유저 팔로우 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserFollowController {
	private UserFollowService userFollowService;


	@Operation(summary = "댓글 좋아요", description = "댓글을 좋아요 토글")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "좋아요 성공"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "404", description = "댓글 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/follow")
	public ResponseEntity<Map<String, Object>> toggleUserFollow(Principal principal, @PathVariable String userId){
		if (false/*JWT인증*/) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "유효하지 않은 인증 정보입니다."));
		}

		if (false/*본인 댓글인지 확인해서 403에러 발생 자기 댓글 좋아요는 불가능*/) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "이 댓글을 수정할 권한이 없습니다. 작성자만 수정할 수 있습니다."));

		}

		if (false/*유저 확인*/) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		String loginUserId = "user_001";//jwt구현시 제거

		Map<String,Object> response = userFollowService.followingUser(userId,loginUserId);

		return ResponseEntity.status(HttpStatus.OK).body(response);


	}

}
