package sns.pinocchio.presentation.post.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum PostErrorCode {
  POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404", "게시글을 찾을 수 없습니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "POST_403", "해당 게시물은 비공개 상태입니다."),
  UNAUTHORIZED_POST_ACCESS(HttpStatus.FORBIDDEN, "POST_403", "게시글에 대한 권한이 없습니다."),
  POST_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "POST_500", "게시글 작성에 실패했습니다."),
  INVALID_POST_CONTENT(HttpStatus.BAD_REQUEST, "POST_400", "게시글 내용이 올바르지 않습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
