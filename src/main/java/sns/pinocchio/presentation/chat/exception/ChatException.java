package sns.pinocchio.presentation.chat.exception;

import org.springframework.http.HttpStatus;

public class ChatException extends RuntimeException {

  private final ChatErrorCode chatErrorCode;

  public ChatException(ChatErrorCode chatErrorCode) {
    super(chatErrorCode.getMessage());
    this.chatErrorCode = chatErrorCode;
  }

  public ChatErrorCode getChatErrorCode() {
    return chatErrorCode;
  }

  public HttpStatus getStatus() {
    return chatErrorCode.getHttpStatus();
  }

  public String getCode() {
    return chatErrorCode.getCode();
  }
}
