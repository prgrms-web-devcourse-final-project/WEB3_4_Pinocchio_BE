package sns.pinocchio.presentation.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.auth.AuthService;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.request.ChangePasswordRequestDto;
import sns.pinocchio.application.member.memberDto.request.DeleteRequestDto;
import sns.pinocchio.application.member.memberDto.request.ResetPasswordRequestDto;
import sns.pinocchio.application.member.memberDto.request.UpdateRequestDto;
import sns.pinocchio.application.member.memberDto.response.ProfileResponseDto;
import sns.pinocchio.application.report.ReportService;
import sns.pinocchio.application.report.reportDto.ReportRequestDto;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;

@RequestMapping("/member")
@RequiredArgsConstructor
@RestController
public class MemberController {

  private final MemberService memberService;
  private final AuthService authService;
  private final ReportService reportService;

  // 유저 프로필 조회
  @GetMapping
  public ResponseEntity<ProfileResponseDto> getProfile(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Member member = userDetails.getMember();

    // 응답 DTO 변환
    ProfileResponseDto profileResponseDto =
        new ProfileResponseDto(
            "success",
            HttpStatus.OK.value(),
            "유저 조회에 성공했습니다.",
            ProfileResponseDto.UserData.of(member));

    return ResponseEntity.status(HttpStatus.OK).body(profileResponseDto);
  }

  // 유저 프로필 수정
  @PutMapping
  public ResponseEntity<ProfileResponseDto> updateMemberInfo(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody UpdateRequestDto updateRequestDto) {
    // 유저 프로필 수정
    Member member = memberService.updateProfile(customUserDetails.getUserId(), updateRequestDto);

    // 응답 DTO 변환
    ProfileResponseDto profileResponseDto =
        new ProfileResponseDto(
            "success",
            HttpStatus.OK.value(),
            "회원 정보 수정에 성공했습니다.",
            ProfileResponseDto.UserData.of(member));

    return ResponseEntity.status(HttpStatus.OK).body(profileResponseDto);
  }

  // 임시 비밀번호 저장 및 발송
  @PostMapping("/password/reset")
  public ResponseEntity<String> sendTemporaryPassword(
      @Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
    memberService.sendTemporaryPassword(resetPasswordRequestDto.email());
    return ResponseEntity.ok("임시 비밀번호가 이메일로 발송되었습니다. 로그인 후 반드시 비밀번호를 변경해 주세요.");
  }

  // 비밀번호 변경
  @PutMapping("/password")
  public ResponseEntity<String> changePassword(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
    // 유저 조회
    Member member = customUserDetails.getMember();

    // 패스워드 검증
    authService.validatePassword(changePasswordRequestDto.currentPassword(), member);

    // 패스워드 변경
    memberService.changePassword(member, changePasswordRequestDto.newPassword());

    return ResponseEntity.status(HttpStatus.OK).body("비밀번호가 성공적으로 변경되었습니다.");
  }

  // 회원 탈퇴
  @DeleteMapping
  public ResponseEntity<String> deleteMember(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      HttpServletRequest request,
      HttpServletResponse response,
      @Valid @RequestBody DeleteRequestDto deleteRequestDto) {
    // 유저 조회
    Member member = customUserDetails.getMember();

    // 패스워드 검증
    authService.validatePassword(deleteRequestDto.password(), member);

    // 유저 삭제
    memberService.deleteMember(member);

    // 쿠키 삭제
    memberService.tokenClear(request, response);

    return ResponseEntity.status(HttpStatus.OK).body("회원 탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.");
  }

  // 계정 신고
  @PostMapping("/report")
  public ResponseEntity<String> reportMember(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody ReportRequestDto reportRequestDto) {
    // 신고자 조회 (사용자 조회)
    Member reporter = customUserDetails.getMember();

    // 신고 대상 조회 (닉네임 유니크)
    Member reported = memberService.findByNickname(reportRequestDto.reportedNickname());

    // 신고 내역 저장
    reportService.createReport(
        reporter.getId(),
        reported.getId(),
        reportRequestDto.reportedType(),
        reportRequestDto.reason());

    return ResponseEntity.ok("계정 신고가 완료되었습니다. 신고 내용은 검토 후 처리됩니다.");
  }
}
