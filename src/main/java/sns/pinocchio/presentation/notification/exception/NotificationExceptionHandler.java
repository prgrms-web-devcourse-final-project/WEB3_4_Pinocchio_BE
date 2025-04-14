package sns.pinocchio.presentation.notification.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationExceptionHandler {

  @ExceptionHandler(NotificationException.class)
  public ResponseEntity<Map<String, Object>> handleNotificationException(NotificationException e) {
    NotificationErrorCode errorCode = e.getNotificationErrorCode();
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(
            Map.of(
                "status", "error",
                "statusCode", errorCode.getHttpStatus().value(),
                "message", errorCode.getMessage(),
                "code", errorCode.getCode()));
  }
}
