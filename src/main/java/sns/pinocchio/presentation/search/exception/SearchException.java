package sns.pinocchio.presentation.search.exception;

import org.springframework.http.HttpStatus;

public class SearchException extends RuntimeException {

  private final SearchErrorCode searchErrorCode;

  public SearchException(SearchErrorCode searchErrorCode) {
    super(searchErrorCode.getMessage());
    this.searchErrorCode = searchErrorCode;
  }

  public SearchErrorCode getSearchErrorCode() {
    return searchErrorCode;
  }

  public HttpStatus getStatus() {
    return searchErrorCode.getHttpStatus();
  }

  public String getCode() {
    return searchErrorCode.getCode();
  }
}
