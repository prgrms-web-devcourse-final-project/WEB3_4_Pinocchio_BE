package sns.pinocchio.presentation.search.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SearchErrorCode {
  EMPTY_SEARCH_QUERY(HttpStatus.BAD_REQUEST, "SEARCH_400", "검색어를 입력해주세요."),
  SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SEARCH_500", "검색 중 오류가 발생했습니다."),
  INVALID_SEARCH_TYPE(HttpStatus.BAD_REQUEST, "SEARCH_401", "잘못된 검색 유형입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
