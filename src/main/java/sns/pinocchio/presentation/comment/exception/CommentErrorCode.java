package sns.pinocchio.presentation.comment.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

// member 관련 예외 코드
@AllArgsConstructor
@Getter
public enum CommentErrorCode {
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT_404", "댓글을 찾을 수 없습니다."),
  UNAUTHORIZED_COMMENT_ACCESS(HttpStatus.FORBIDDEN, "COMMENT_403", "댓글에 대한 권한이 없습니다."),
  COMMENT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMENT_500", "댓글 작성에 실패했습니다."),
  INVALID_COMMENT_CONTENT(HttpStatus.BAD_REQUEST, "COMMENT_400", "댓글 내용이 올바르지 않습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
