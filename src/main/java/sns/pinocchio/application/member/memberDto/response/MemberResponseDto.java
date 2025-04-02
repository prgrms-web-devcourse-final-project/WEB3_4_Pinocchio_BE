package sns.pinocchio.application.member.memberDto.response;

import lombok.Builder;
import sns.pinocchio.domain.member.Member;

// 사용자 기본 정보 조회 응답 Dto
@Builder
public record MemberResponseDto(
    String email,
    String name,
    String nickname,
    String bio,
    String website,
    String profileImageUrl,
    Boolean isActive) {
  public static MemberResponseDto of(Member member) {
    return MemberResponseDto.builder()
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
