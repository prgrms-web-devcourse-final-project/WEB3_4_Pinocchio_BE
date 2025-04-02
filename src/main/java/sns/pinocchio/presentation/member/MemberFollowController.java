package sns.pinocchio.presentation.member;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.member.memberDto.MemberFollowRequest;
import sns.pinocchio.application.member.MemberFollowService;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;

@Tag(name = "유저 팔로우", description = "유저 팔로우 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberFollowController {
	private final MemberFollowService memberFollowService;
	private final MemberRepository memberRepository;

	@Operation(summary = "유저 팔로우", description = "유저 팔로우 토글")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "팔로우 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 값"),
		@ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/follow")
	public ResponseEntity<Map<String, Object>> toggleUserFollow(Principal principal, @PathVariable String userId,
		@RequestBody MemberFollowRequest request) {
		Optional<Member> optMember = memberRepository.findByName(principal.getName());
		if (optMember.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Member authorMember = optMember.get();

		if (Objects.equals(authorMember.getTsid(), userId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(Map.of("message", "스스로 팔로잉 불가능"));

		}

		Map<String, Object> response = memberFollowService.followingUser(request, userId, authorMember.getTsid(),
			authorMember.getNickname());

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "유저 팔로워 목록", description = "유저 팔로워 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/followers")
	public ResponseEntity<Map<String, Object>> findFollowers(@PathVariable String userId,
		@RequestParam(value = "page", defaultValue = "0") int page) {
		Optional<Member> optMember = memberRepository.findByTsid(userId);
		if (optMember.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Map<String, Object> response = memberFollowService.findFollowers(userId, page);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@Operation(summary = "유저 팔로잉 목록", description = "유저 팔로잉 목록 가져오기")
	@ApiResponses({@ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "유저 조회 실패"),
		@ApiResponse(responseCode = "500", description = "서버 내부 오류")})
	@PostMapping("/{userId}/followings")
	public ResponseEntity<Map<String, Object>> findFollowings(@PathVariable String userId,
		@RequestParam(value = "page", defaultValue = "0") int page) {
		Optional<Member> optMember = memberRepository.findByTsid(userId);
		if (optMember.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "등록된 유저를 찾을 수 없습니다."));
		}

		Map<String, Object> response = memberFollowService.findFollowings(userId, page);

		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
