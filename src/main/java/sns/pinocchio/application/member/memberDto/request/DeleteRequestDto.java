package sns.pinocchio.application.member.memberDto.request;

import jakarta.validation.constraints.NotBlank;

// 회원 탈퇴 요청 Dto
public record DeleteRequestDto(@NotBlank(message = "비밀번호는 필수 항목입니다.") String password) {}
