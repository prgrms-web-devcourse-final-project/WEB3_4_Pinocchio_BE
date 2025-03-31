package sns.pinocchio.application.member.memberDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 임시 패스워드 요청 Dto
public record ResetPasswordRequestDto(
    @NotBlank(message = "이메일은 필수 항목입니다.") @Email(message = "유효한 이메일 형식이 아닙니다.") String email) {}
