package sns.pinocchio.presentation.search.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class SearchExceptionHandler {

  @ExceptionHandler(SearchException.class)
  public ResponseEntity<Map<String, Object>> handleSearchException(SearchException e) {
    SearchErrorCode errorCode = e.getSearchErrorCode();
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(
            Map.of(
                "status", "error",
                "statusCode", errorCode.getHttpStatus().value(),
                "message", errorCode.getMessage(),
                "code", errorCode.getCode()));
  }
}
