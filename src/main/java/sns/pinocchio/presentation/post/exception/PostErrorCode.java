package sns.pinocchio.presentation.post.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum PostErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404", "게시물을 찾을 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "POST_403", "해당 게시물은 비공개 상태입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}