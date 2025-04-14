package sns.pinocchio.presentation.search.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SearchException extends RuntimeException {

  private final SearchErrorCode searchErrorCode;

  public SearchException(SearchErrorCode searchErrorCode) {
    super(searchErrorCode.getMessage());
    this.searchErrorCode = searchErrorCode;
  }

  public HttpStatus getStatus() {
    return searchErrorCode.getHttpStatus();
  }

  public String getCode() {
    return searchErrorCode.getCode();
  }
}
