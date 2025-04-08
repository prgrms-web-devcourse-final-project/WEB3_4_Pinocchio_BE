package sns.pinocchio.presentation.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ChatErrorCode {
  CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "채팅방을 찾을 수 없습니다."),
  MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_500", "메시지 전송에 실패했습니다."),
  UNAUTHORIZED_CHAT_ACCESS(HttpStatus.FORBIDDEN, "CHAT_403", "채팅방에 접근할 수 있는 권한이 없습니다."),
  INVALID_CHAT_REQUEST(HttpStatus.BAD_REQUEST, "CHAT_400", "잘못된 채팅 요청입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
