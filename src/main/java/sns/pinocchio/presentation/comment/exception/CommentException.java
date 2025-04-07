package sns.pinocchio.presentation.comment.exception;

import org.springframework.http.HttpStatus;

// member 관련 예외 처리
public class CommentException extends RuntimeException {

  private final CommentErrorCode memberErrorCode;

  public CommentException(CommentErrorCode memberErrorCode) {
    super(memberErrorCode.getMessage());
    this.memberErrorCode = memberErrorCode;
  }

  public CommentErrorCode getMemberErrorCode() {
    return memberErrorCode;
  }

  public HttpStatus getStatus() {
    return memberErrorCode.getHttpStatus();
  }

  public String getCode() {
    return memberErrorCode.getCode();
  }
}
