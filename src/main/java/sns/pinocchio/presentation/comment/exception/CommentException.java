package sns.pinocchio.presentation.comment.exception;

import org.springframework.http.HttpStatus;

public class CommentException extends RuntimeException {

  private final CommentErrorCode commentErrorCode;

  public CommentException(CommentErrorCode commentErrorCode) {
    super(commentErrorCode.getMessage());
    this.commentErrorCode = commentErrorCode;
  }

  public CommentErrorCode getCommentErrorCode() {
    return commentErrorCode;
  }

  public HttpStatus getStatus() {
    return commentErrorCode.getHttpStatus();
  }

  public String getCode() {
    return commentErrorCode.getCode();
  }
}
