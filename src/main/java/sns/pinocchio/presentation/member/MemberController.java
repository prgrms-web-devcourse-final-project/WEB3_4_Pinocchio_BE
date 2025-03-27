package sns.pinocchio.presentation.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.member.MemberService;

@RequestMapping("/member")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    // 회원 정보 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<String> getMemberInfo(@PathVariable Long memberId) {
        return ResponseEntity.ok("회원 정보 조회 성공");
    }

    // 회원 정보 수정
    @PutMapping("/{memberId}")
    public ResponseEntity<String> updateMemberInfo(@PathVariable Long memberId) {
        return ResponseEntity.ok("회원 정보 수정 성공");
    }

    // 임시 비밀번호 발송
    @PostMapping("/{memberId}/password/reset")
    public ResponseEntity<String> sendTemporaryPassword(@PathVariable Long memberId) {
        return ResponseEntity.ok("임시 비밀번호 발송 성공");
    }

    // 비밀번호 변경
    @PutMapping("/{memberId}/password")
    public ResponseEntity<String> changePassword(@PathVariable Long memberId) {
        return ResponseEntity.ok("비밀번호 변경 성공");
    }

    // 회원 탈퇴
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable Long memberId) {
        return ResponseEntity.ok("회원 탈퇴 성공");
    }

    // 계정 신고
    @PostMapping("/report")
    public ResponseEntity<String> reportMember(@RequestParam Long targetMemberId) {
        return ResponseEntity.ok("계정 신고 성공");
    }

    // 계정 차단
    @PostMapping("/block")
    public ResponseEntity<String> blockMember(@RequestParam Long targetMemberId) {
        return ResponseEntity.ok("계정 차단 성공");
    }

    // 계정 차단 해제
    @PostMapping("/unblock")
    public ResponseEntity<String> unblockMember(@RequestParam Long targetMemberId) {
        return ResponseEntity.ok("계정 차단 해제 성공");
    }

    // 차단한 유저 조회
    @GetMapping("/block")
    public ResponseEntity<String> getBlockedMembers(@RequestParam Long memberId) {
        return ResponseEntity.ok("차단한 유저 조회 성공");
    }
}