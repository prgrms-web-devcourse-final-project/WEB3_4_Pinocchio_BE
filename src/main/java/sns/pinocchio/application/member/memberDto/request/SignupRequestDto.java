package sns.pinocchio.application.member.memberDto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// 회원가입 요청 Dto
public record SignupRequestDto(
    @NotBlank(message = "이름은 필수 항목입니다.") String name,
    @NotBlank(message = "이메일은 필수 항목입니다.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
    @NotBlank(message = "닉네임은 필수 항목입니다.")
        @Size(min = 3, max = 20, message = "닉네임은 3자 이상 20자 이하로 입력해주세요.")
        String nickname,
    @NotBlank(message = "비밀번호는 필수 항목입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        @Pattern(
            regexp = "^(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 특수문자를 포함해야 합니다.")
        String password) {}
