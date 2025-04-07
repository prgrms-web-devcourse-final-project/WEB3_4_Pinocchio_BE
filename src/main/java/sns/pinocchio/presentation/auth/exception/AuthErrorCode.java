package sns.pinocchio.presentation.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// auth 관련 예외 코드
@AllArgsConstructor
@Getter
public enum AuthErrorCode {
  AUTHORIZATION_FAILED(HttpStatus.UNAUTHORIZED, "401-1", "인증에 실패했습니다."),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "401-2", "토큰이 만료되었습니다. 다시 로그인을 부탁드립니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "401-3", "로그인이 필요한 페이지 입니다."),
  TOKEN_REISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "500", "토큰 갱신에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
