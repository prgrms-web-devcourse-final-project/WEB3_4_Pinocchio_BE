package sns.pinocchio.presentation.member.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class MemberExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<Map<String, Object>> handleMemberException(MemberException ex) {
        MemberErrorCode errorCode = ex.getMemberErrorCode();

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
