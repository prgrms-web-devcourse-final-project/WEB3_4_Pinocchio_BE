package sns.pinocchio.application.notification.dto;

import lombok.Builder;

public class NotificationResponseDto {

  @Builder
  public record NotificationInfo(
      String userId,
      boolean message,
      boolean like,
      boolean comment,
      boolean follow,
      boolean mention) {}
}