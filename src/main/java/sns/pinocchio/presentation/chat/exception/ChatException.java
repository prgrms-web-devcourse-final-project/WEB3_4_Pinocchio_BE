package sns.pinocchio.presentation.chat.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChatException extends RuntimeException {

  private final ChatErrorCode chatErrorCode;

  public ChatException(ChatErrorCode chatErrorCode) {
    super(chatErrorCode.getMessage());
    this.chatErrorCode = chatErrorCode;
  }

  public HttpStatus getStatus() {
    return chatErrorCode.getHttpStatus();
  }

  public String getCode() {
    return chatErrorCode.getCode();
  }
}
