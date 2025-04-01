package sns.pinocchio.domain.notification;

import sns.pinocchio.infrastructure.shared.exception.BaseException;

public class NotificationException {

  private NotificationException() {}

  public static class NotificationBadRequestException extends BaseException {
    public NotificationBadRequestException(String message) {
      super(message, 400);
    }
  }

  public static class NotificationInternalServerErrorException extends BaseException {
    public NotificationInternalServerErrorException(String message) {
      super(message, 500);
    }
  }
}
