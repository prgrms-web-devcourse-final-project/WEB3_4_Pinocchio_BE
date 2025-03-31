package sns.pinocchio.domain.chatroom;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BlockedInfo {

  private boolean isBlocked; // 채팅방 차단 유무

  private String blockedBy; // 채팅방을 차단한 참가자 ID
}
