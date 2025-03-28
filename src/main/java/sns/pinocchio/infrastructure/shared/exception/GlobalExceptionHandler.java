package sns.pinocchio.infrastructure.shared.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sns.pinocchio.infrastructure.shared.response.GlobalApiResponse;

import java.util.List;

@Hidden
@RestControllerAdvice(
    annotations = {RestController.class},
    basePackages = "sns.pinocchio.presentation")
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<GlobalApiResponse<List<Object>>> handleBaseException(BaseException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(GlobalApiResponse.error(ex.getStatusCode(), ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<GlobalApiResponse<List<Object>>> handleUnknown() {

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(GlobalApiResponse.error(500, "알 수 없는 서버 오류가 발생했습니다."));
  }
}
