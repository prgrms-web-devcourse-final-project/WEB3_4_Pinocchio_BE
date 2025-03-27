package sns.pinocchio.domain.notification.exception;

import sns.pinocchio.shared.exception.BaseException;

public class NotificationBadRequestException extends BaseException {

  public NotificationBadRequestException(String message) {
    super(message, 400);
  }
}
