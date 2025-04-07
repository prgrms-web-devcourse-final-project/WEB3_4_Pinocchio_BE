package sns.pinocchio.presentation.notification.exception;

import org.springframework.http.HttpStatus;

public class NotificationException extends RuntimeException {

  private final NotificationErrorCode notificationErrorCode;

  public NotificationException(NotificationErrorCode notificationErrorCode) {
    super(notificationErrorCode.getMessage());
    this.notificationErrorCode = notificationErrorCode;
  }

  public NotificationErrorCode getNotificationErrorCode() {
    return notificationErrorCode;
  }

  public HttpStatus getStatus() {
    return notificationErrorCode.getHttpStatus();
  }

  public String getCode() {
    return notificationErrorCode.getCode();
  }
}
