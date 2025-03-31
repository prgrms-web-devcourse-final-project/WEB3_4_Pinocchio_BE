package sns.pinocchio.application.member.memberDto;

import lombok.Builder;
import sns.pinocchio.domain.member.Member;

// 사용자 기본 정보 조회 응답 dto
public record ProfileResponseDto(String status, int statusCode, String message, UserData data) {
  @Builder
  public record UserData(
      String email,
      String name,
      String nickname,
      String bio,
      String website,
      String profileImageUrl,
      Boolean isActive) {
    public static UserData of(Member member) {
      return UserData.builder()
          .email(member.getEmail())
          .name(member.getName())
          .nickname(member.getNickname())
          .bio(member.getBio())
          .website(member.getWebsite())
          .profileImageUrl(member.getProfileImageUrl())
          .isActive(member.getIsActive())
          .build();
    }
  }
}
