package sns.pinocchio.presentation.block.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum BlockErrorCode {
  ALREADY_BLOCKED(HttpStatus.CONFLICT, "BLOCK_409", "이미 차단한 사용자입니다."),
  CANNOT_BLOCK_SELF(HttpStatus.BAD_REQUEST, "BLOCK_400", "자기 자신을 차단할 수 없습니다."),
  BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK_404", "차단 정보를 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
