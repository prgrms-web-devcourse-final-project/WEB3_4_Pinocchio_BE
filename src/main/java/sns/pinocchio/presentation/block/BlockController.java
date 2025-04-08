package sns.pinocchio.presentation.block;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.blockedUser.BlockedUserService;
import sns.pinocchio.application.blockedUser.response.BlockedUserResponse;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;

@RequestMapping("/block")
@RequiredArgsConstructor
@RestController
public class BlockController {

  private final BlockedUserService blockedUserService;

  // 유저 차단
  @PostMapping("/{userId}")
  public ResponseEntity<String> blockMember(
      @PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Member member = customUserDetails.getMember();

    blockedUserService.saveBlock(member.getId(), userId);

    return ResponseEntity.ok("해당 회원의 계정이 차단되었습니다.");
  }

  // 유저 차단 해제
  @DeleteMapping("/{userId}")
  public ResponseEntity<String> unblockMember(
      @PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Member member = customUserDetails.getMember();

    blockedUserService.deleteBlock(member.getId(), userId);

    return ResponseEntity.ok("해당 회원의 계정 차단이 해제되었습니다.");
  }

  // 차단한 유저 조회
  @GetMapping
  public ResponseEntity<BlockedUserResponse> getBlockedMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Member member = customUserDetails.getMember();
    List<BlockedUserResponse.UserData> blockedUsers =
        blockedUserService.getBlockedUsers(member.getId());

    BlockedUserResponse response =
        BlockedUserResponse.builder()
            .status("success")
            .statusCode(HttpStatus.OK.value())
            .message("차단한 회원 목록을 성공적으로 조회했습니다.")
            .data(blockedUsers)
            .build();

    return ResponseEntity.ok(response);
  }
}
