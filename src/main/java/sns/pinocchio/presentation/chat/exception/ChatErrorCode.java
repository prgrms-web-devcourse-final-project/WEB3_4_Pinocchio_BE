package sns.pinocchio.presentation.chat.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ChatErrorCode {
  UNAUTHORIZED_CHAT_USER(HttpStatus.UNAUTHORIZED, "CHAT_401", "사용자가 인증되지 않았습니다."),
  INVALID_CHAT_REQUEST(HttpStatus.BAD_REQUEST, "CHAT_400", "입력값이 유효하지 않습니다."),
  CHAT_SENDER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "송신자의 회원 정보를 찾을 수 없습니다."),
  CHAT_RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "수신자의 회원 정보를 찾을 수 없습니다."),
  MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_500", "메시지 전송에 실패했습니다."),
  MESSAGE_ALERT_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CHAT_500", "메시지 알림 전송에 실패했습니다."),
  CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "등록된 채팅방을 찾을 수 없습니다."),
  CHAT_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_404", "상대방 사용자의 정보가 존재하지 않습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
