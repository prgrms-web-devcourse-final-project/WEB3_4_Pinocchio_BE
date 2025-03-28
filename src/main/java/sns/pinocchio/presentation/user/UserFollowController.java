package sns.pinocchio.presentation.user;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.user.UserFollowRequest;
import sns.pinocchio.application.user.UserFollowService;

@Tag(name = "유저 팔로우", description = "유저 팔로우 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserFollowController {
	private final UserFollowService userFollowService;

	@Operation(summary = "유저 팔로우", description = "유저 팔로우 토글")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "팔로우 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 값"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/follow")
	public ResponseEntity<Map<String, Object>> toggleUserFollow(Principal principal, @PathVariable String userId,
		@RequestBody UserFollowRequest request) {
		if (false/*JWT인증*/) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "유효하지 않은 인증 정보입니다."));
		}

		if (false/*본인인지 확인해서 400에러 발생 자기 스스로 팔로잉은 불가능*/) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("message", "이 댓글을 수정할 권한이 없습니다. 작성자만 수정할 수 있습니다."));

		}

		if (false/*유저 확인*/) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		String authorId = "user_001";//jwt구현시 제거
		String authorNickname = "고길동";//jwt구현시 제거
		Map<String, Object> response = userFollowService.followingUser(request, userId, authorId, authorNickname);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "유저 팔로워 목록", description = "유저 팔로워 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/followers")
	public ResponseEntity<Map<String, Object>> findFollowers(@PathVariable String userId) {
		if (false/*유저 확인*/) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Map<String, Object> response = userFollowService.findFollowers(userId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "유저 팔로잉 목록", description = "유저 팔로잉 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/followings")
	public ResponseEntity<Map<String, Object>> findFollowings(@PathVariable String userId) {
		if (false/*유저 확인*/) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Map<String, Object> response = userFollowService.findFollowings(userId);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
