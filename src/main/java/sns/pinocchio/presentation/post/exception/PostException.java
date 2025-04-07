package sns.pinocchio.presentation.post.exception;

import org.springframework.http.HttpStatus;

public class PostException extends RuntimeException {

  private final PostErrorCode postErrorCode;

  public PostException(PostErrorCode postErrorCode) {
    super(postErrorCode.getMessage());
    this.postErrorCode = postErrorCode;
  }

  public PostErrorCode getPostErrorCode() {
    return postErrorCode;
  }

  public HttpStatus getStatus() {
    return postErrorCode.getHttpStatus();
  }

  public String getCode() {
    return postErrorCode.getCode();
  }
}
