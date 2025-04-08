package sns.pinocchio.config.global.exceptionHandler;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sns.pinocchio.application.base.errorResponse.ErrorResponse;
import sns.pinocchio.presentation.auth.exception.AuthErrorCode;
import sns.pinocchio.presentation.auth.exception.AuthException;
import sns.pinocchio.presentation.block.exception.BlockErrorCode;
import sns.pinocchio.presentation.block.exception.BlockException;
import sns.pinocchio.presentation.chat.exception.ChatErrorCode;
import sns.pinocchio.presentation.chat.exception.ChatException;
import sns.pinocchio.presentation.comment.exception.CommentErrorCode;
import sns.pinocchio.presentation.comment.exception.CommentException;
import sns.pinocchio.presentation.mail.exception.MailErrorCode;
import sns.pinocchio.presentation.mail.exception.MailException;
import sns.pinocchio.presentation.member.exception.MemberErrorCode;
import sns.pinocchio.presentation.member.exception.MemberException;
import sns.pinocchio.presentation.notification.exception.NotificationErrorCode;
import sns.pinocchio.presentation.notification.exception.NotificationException;
import sns.pinocchio.presentation.post.exception.PostErrorCode;
import sns.pinocchio.presentation.post.exception.PostException;
import sns.pinocchio.presentation.report.exception.ReportErrorCode;
import sns.pinocchio.presentation.report.exception.ReportException;
import sns.pinocchio.presentation.search.exception.SearchErrorCode;
import sns.pinocchio.presentation.search.exception.SearchException;

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

  // BlockException 처리
  @ExceptionHandler(BlockException.class)
  public ResponseEntity<ErrorResponse> handleBlockException(BlockException ex) {
    BlockErrorCode errorCode = ex.getBlockErrorCode();

    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    // HTTP 상태 코드와 함께 응답 반환
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // ChatException 처리
  @ExceptionHandler(ChatException.class)
  public ResponseEntity<ErrorResponse> handleChatException(ChatException ex) {
    ChatErrorCode errorCode = ex.getChatErrorCode();

    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    // HTTP 상태 코드와 함께 응답 반환
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // CommentException 처리
  @ExceptionHandler(CommentException.class)
  public ResponseEntity<ErrorResponse> handleCommentException(CommentException ex) {
    CommentErrorCode errorCode = ex.getCommentErrorCode();

    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    // HTTP 상태 코드와 함께 응답 반환
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // MailException 처리
  @ExceptionHandler(MailException.class)
  public ResponseEntity<ErrorResponse> handleMailException(MailException ex) {
    MailErrorCode errorCode = ex.getMailErrorCode();

    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    // HTTP 상태 코드와 함께 응답 반환
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // NotificationException 처리
  @ExceptionHandler(NotificationException.class)
  public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException ex) {
    NotificationErrorCode errorCode = ex.getNotificationErrorCode();

    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());

    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // PostException 처리
  @ExceptionHandler(PostException.class)
  public ResponseEntity<ErrorResponse> handlePostException(PostException ex) {
    PostErrorCode errorCode = ex.getPostErrorCode();
    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // SearchException 처리
  @ExceptionHandler(SearchException.class)
  public ResponseEntity<ErrorResponse> handleSearchException(SearchException ex) {
    SearchErrorCode errorCode = ex.getSearchErrorCode();
    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());
    return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
  }

  // ReportException 처리
  @ExceptionHandler(ReportException.class)
  public ResponseEntity<ErrorResponse> handleReportException(ReportException ex) {
    ReportErrorCode errorCode = ex.getReportErrorCode();
    ErrorResponse errorResponse =
        new ErrorResponse(
            "error",
            errorCode.getHttpStatus().value(),
            errorCode.getMessage(),
            errorCode.getCode());
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
