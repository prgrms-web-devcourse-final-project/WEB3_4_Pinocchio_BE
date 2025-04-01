package sns.pinocchio.application.member.memberDto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 패스워드 변경 요청 Dto
public record ChangePasswordRequestDto(
    @NotBlank(message = "현재 비밀번호는 필수 항목입니다.") String currentPassword,
    @NotBlank(message = "새 비밀번호는 필수 항목입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(
            regexp = "^(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 특수문자를 포함해야 합니다.")
        String newPassword) {}
