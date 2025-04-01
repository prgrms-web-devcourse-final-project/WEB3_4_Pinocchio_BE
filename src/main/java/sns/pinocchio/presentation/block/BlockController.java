package sns.pinocchio.presentation.block;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.blockedUser.BlockedUserService;
import sns.pinocchio.application.blockedUser.request.BlockRequestDto;
import sns.pinocchio.domain.member.Member;

@RequestMapping("/block")
@RequiredArgsConstructor
@RestController
public class BlockController {

  private final BlockedUserService blockedUserService;

  // 유저 차단
  @PostMapping("/user")
  public ResponseEntity<String> blockMember(
      @RequestBody BlockRequestDto blockRequestDto, @AuthenticationPrincipal Member member) {
    return ResponseEntity.ok("해당 회원의 계정이 차단되었습니다.");
  }

  // 유저 차단 해제
  @DeleteMapping("{userId}")
  public ResponseEntity<String> unblockMember(
      @PathVariable Long memberId, @AuthenticationPrincipal Member member) {

    return ResponseEntity.ok("해당 회원의 계정 차단이 해제되었습니다.");
  }

  // 차단한 유저 조회
  @GetMapping("/users")
  public ResponseEntity<String> getBlockedMembers(@AuthenticationPrincipal Member member) {

    return ResponseEntity.ok("차단한 회원 목록을 성공적으로 조회했습니다.");
  }
}
