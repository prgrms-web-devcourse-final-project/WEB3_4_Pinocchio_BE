package sns.pinocchio.config.global.ExceptionHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sns.pinocchio.application.base.errorResponse.ErrorResponse;
import sns.pinocchio.presentation.member.exception.MemberException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<String> handleMemberException(MemberException ex) {
        // 예외에 맞는 HTTP 상태 코드와 메시지 반환
        return ResponseEntity
                .status(ex.getStatus())  // 예외에서 정의한 HTTP 상태 코드 반환
                .body(ex.getMessage());  // 예외에서 정의한 메시지 반환
    }

    // 다른 예외들도 일관된 형태로 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "입력 값 검증 실패",
                errors
        );

        return ResponseEntity
                .badRequest()
                .body(errorResponse);
    }
}
