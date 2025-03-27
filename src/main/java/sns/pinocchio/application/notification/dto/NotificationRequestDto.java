package sns.pinocchio.application.notification.dto;

import lombok.Builder;

public class NotificationRequestDto {

  @Builder
  public record UpdateNotifications(
      Boolean message, Boolean like, Boolean comment, Boolean follow, Boolean mention) {

    public boolean checkNotifications() {
      return message != null
          && like != null
          && comment != null
          && follow != null
          && mention != null;
    }
  }
}
