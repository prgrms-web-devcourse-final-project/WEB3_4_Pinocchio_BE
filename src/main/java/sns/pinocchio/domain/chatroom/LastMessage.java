package sns.pinocchio.domain.chatroom;

import java.time.Instant;
import lombok.*;
import sns.pinocchio.domain.chat.ChatStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LastMessage {

  private String senderId; // 발신자 ID

  private String content; // 메시지 내용

  private Instant createdAt; // 메시지 전송 시간

  private boolean readStatus; // 메시지 읽음 여부

  private ChatStatus status; // 메시지 전송 상태 (SENT | DELIVERED | READ)
}
