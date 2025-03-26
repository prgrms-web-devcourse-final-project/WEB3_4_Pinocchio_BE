package sns.pinocchio.application.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class NotificationRequestDto {

  @Schema(description = "알림 설정 변경 요청 DTO")
  @Builder
  public record UpdateNotifications(
      boolean message, boolean like, boolean comment, boolean follow, boolean mention) {}
}
