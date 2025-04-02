package sns.pinocchio.config.global.exceptionHandler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sns.pinocchio.application.base.errorResponse.ErrorResponse;
import sns.pinocchio.presentation.auth.exception.AuthErrorCode;
import sns.pinocchio.presentation.auth.exception.AuthException;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;

import java.util.HashMap;
import java.util.Map;

/*
@ControllerAdvice
public class GlobalExceptionHandler {

  // MemberException 처리
  @ExceptionHandler(MemberException.class)
  public ResponseEntity<ErrorResponse> handleMemberException(MemberException ex) {
    MemberErrorCode errorCode = ex.getMemberErrorCode();

    // ErrorResponse 객체 생성
    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    // HTTP 상태 코드와 함께 응답 반환
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // AuthException 처리
  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
    AuthErrorCode errorCode = ex.getAuthErrorCode();

    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    // HTTP 상태 코드와 함께 응답 반환
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // MethodArgumentNotValidException 처리 (유효성 검사 실패 처리)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ErrorResponse errorResponse =
        new ErrorResponse(
            "VALIDATION_ERROR", // 상태
            400, // 상태 코드
            "입력 값 검증 실패", // 메시지
            null // 코드 (사용하지 않음)
            );

    return ResponseEntity.badRequest().body(errorResponse);
  }
}
*/
