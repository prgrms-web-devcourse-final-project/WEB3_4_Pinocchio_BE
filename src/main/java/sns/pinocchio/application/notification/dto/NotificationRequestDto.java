package sns.pinocchio.application.notification.dto;

import lombok.Builder;

public class NotificationRequestDto {

  @Builder
  public record UpdateNotifications(
      boolean message, boolean like, boolean comment, boolean follow, boolean mention) {}
}
