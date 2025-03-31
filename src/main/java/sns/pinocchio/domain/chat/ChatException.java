package sns.pinocchio.domain.chat;

import sns.pinocchio.infrastructure.shared.exception.BaseException;

public class ChatException {

  private ChatException() {}

  public static class ChatBadRequestException extends BaseException {
    public ChatBadRequestException(String message) {
      super(message, 400);
    }
  }

  public static class ChatNotFoundException extends BaseException {
    public ChatNotFoundException(String message) {
      super(message, 404);
    }
  }

  public static class ChatInternalServerErrorException extends BaseException {
    public ChatInternalServerErrorException(String message) {
      super(message, 500);
    }
  }
}
