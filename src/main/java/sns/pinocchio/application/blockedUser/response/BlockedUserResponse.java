package sns.pinocchio.application.blockedUser.response;

import java.util.List;
import lombok.Builder;
import sns.pinocchio.domain.member.Member;

@Builder
public record BlockedUserResponse(
    String status, int statusCode, String message, List<BlockedUserResponse.UserData> data) {
  @Builder
  public record UserData(Long userId, String nickname, String profileImageUrl) {
    public static UserData of(Member member) {
      return UserData.builder()
          .userId(member.getId())
          .nickname(member.getNickname())
          .profileImageUrl(member.getProfileImageUrl())
          .build();
    }
  }
}
