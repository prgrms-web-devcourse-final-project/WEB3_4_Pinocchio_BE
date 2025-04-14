package sns.pinocchio.presentation.notification.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotificationException extends RuntimeException {

  private final NotificationErrorCode notificationErrorCode;

  public NotificationException(NotificationErrorCode notificationErrorCode) {
    super(notificationErrorCode.getMessage());
    this.notificationErrorCode = notificationErrorCode;
  }

  public HttpStatus getStatus() {
    return notificationErrorCode.getHttpStatus();
  }

  public String getCode() {
    return notificationErrorCode.getCode();
  }
}
