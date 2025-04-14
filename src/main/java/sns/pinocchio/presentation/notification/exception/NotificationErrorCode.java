package sns.pinocchio.presentation.notification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum NotificationErrorCode {
  INVALID_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST, "NOTI_400", "입력값이 유효하지 않습니다."),
  UNAUTHORIZED_NOTIFICATION_USER(HttpStatus.UNAUTHORIZED, "NOTI_401", "사용자가 인증되지 않았습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
