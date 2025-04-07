package sns.pinocchio.presentation.comment.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

// member 관련 예외 코드
@AllArgsConstructor
@Getter
public enum CommentErrorCode {
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "등록된 댓글을 찾을 수 없습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "COMMENT_403", "권한이 없습니다."),
  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMENT_400", "잘못된 요청입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
