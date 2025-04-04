package sns.pinocchio.presentation.auth.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuthException(AuthException ex) {
        AuthErrorCode errorCode = ex.getAuthErrorCode();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(Map.of(
                        "status", "error",
                        "statusCode", errorCode.getHttpStatus().value(),
                        "message", errorCode.getMessage(),
                        "code", errorCode.getCode()
                ));
    }
}