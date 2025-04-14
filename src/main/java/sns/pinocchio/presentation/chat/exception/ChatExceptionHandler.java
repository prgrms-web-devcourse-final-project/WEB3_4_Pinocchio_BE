package sns.pinocchio.presentation.chat.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChatExceptionHandler {

  @ExceptionHandler(ChatException.class)
  public ResponseEntity<Map<String, Object>> handleChatException(ChatException e) {
    ChatErrorCode errorCode = e.getChatErrorCode();
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(
            Map.of(
                "status", "error",
                "statusCode", errorCode.getHttpStatus().value(),
                "message", errorCode.getMessage(),
                "code", errorCode.getCode()));
  }
}
