package sns.pinocchio.presentation.auth.exception;

import org.springframework.http.HttpStatus;

// auth 관련 예외 처리
public class AuthException extends RuntimeException {

  private final AuthErrorCode authErrorCode;

  public AuthException(AuthErrorCode authErrorCode) {
    super(authErrorCode.getMessage());
    this.authErrorCode = authErrorCode;
  }

  public AuthErrorCode getAuthErrorCode() {
    return authErrorCode;
  }
  
  public HttpStatus getStatus() {
    return authErrorCode.getHttpStatus();
  }

  public String getCode() {
    return authErrorCode.getCode();
  }
}
