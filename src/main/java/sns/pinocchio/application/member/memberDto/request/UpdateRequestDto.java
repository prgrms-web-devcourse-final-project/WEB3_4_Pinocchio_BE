package sns.pinocchio.application.member.memberDto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 사용자 프로필 정보 수정 요청 Dto
public record UpdateRequestDto(
    @NotBlank(message = "이름은 필수 항목입니다.") String name,
    @NotBlank(message = "닉네임은 필수 항목입니다.")
        @Size(min = 3, max = 20, message = "닉네임은 3자 이상 20자 이하로 입력해주세요.")
        String nickname,
    @Size(max = 100, message = "소개글은 100자 이하로 입력해주세요.") String bio,
    String website,
    String profileImageUrl,
    Boolean isActive) {
  public UpdateRequestDto withProfileImageUrl(String imageUrl) {
    return new UpdateRequestDto(
        this.name, this.nickname, this.bio, this.website, imageUrl, this.isActive);
  }
}
