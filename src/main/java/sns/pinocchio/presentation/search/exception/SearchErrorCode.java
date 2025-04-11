package sns.pinocchio.presentation.search.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SearchErrorCode {
  UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "SEARCH_401", "유효하지 않는 인증 정보입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
