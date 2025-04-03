package sns.pinocchio.presentation.post.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class PostExceptionHandler {

    @ExceptionHandler(PostException.class)
    public ResponseEntity<Map<String, Object>> handlePostException(PostException ex) {
        PostErrorCode errorCode = ex.getPostErrorCode();
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