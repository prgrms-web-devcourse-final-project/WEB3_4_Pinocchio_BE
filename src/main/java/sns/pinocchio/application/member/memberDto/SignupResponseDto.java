package sns.pinocchio.application.member.memberDto;

import lombok.Builder;
import sns.pinocchio.domain.member.Member;

import java.time.LocalDateTime;

// 로그인 후 사용자 정보 응답 Dto
public record SignupResponseDto(String status, int statusCode, String message, UserData data) {
  @Builder
  public record UserData(
      Long userId, String name, String nickname, String email, LocalDateTime createdAt) {
    public static UserData of(Member member) {
      return UserData.builder()
          .userId(member.getId())
          .name(member.getName())
          .nickname(member.getNickname())
          .email(member.getEmail())
          .createdAt(member.getCreatedAt())
          .build();
    }
  }
}
