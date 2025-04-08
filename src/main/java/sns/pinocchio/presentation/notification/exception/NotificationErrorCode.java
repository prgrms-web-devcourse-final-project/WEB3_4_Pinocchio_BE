package sns.pinocchio.presentation.notification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum NotificationErrorCode {
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI_404", "알림을 찾을 수 없습니다."),
  UNAUTHORIZED_NOTIFICATION_ACCESS(HttpStatus.FORBIDDEN, "NOTI_403", "알림에 접근할 권한이 없습니다."),
  NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "NOTI_500", "알림 전송에 실패했습니다."),
  INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTI_400", "유효하지 않은 알림 타입입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
