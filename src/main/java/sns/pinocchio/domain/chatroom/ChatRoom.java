package sns.pinocchio.domain.chatroom;

import jakarta.persistence.Id;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "chatrooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ChatRoom {

  @Id private String id; // 채팅방 ID

  @Indexed private String tsid; // 채팅방 TSID

  @Indexed private List<String> participantTsids; // 채팅방 참여자 리스트 (참가자 TSID)

  private Instant createdAt; // 채팅방 생성 날짜

  @Indexed private String createdAtTsid; // 채팅방 생성 날짜 TSID

  private LastMessage lastMessage; // 마지막 메시지 정보

  private Map<String, Integer> unreadCounts; // 읽지 않은 메시지 수 (key: 참가자 ID, value: 읽지 않은 메시지 수)

  private ChatRoomStatus status; // 채팅방 상태 (PENDING | READ_ONLY | ACTIVE)

  private BlockedInfo blocked; // 채팅방 차단 상태

  private List<String> deletedBy; // 채팅방을 삭제한 참여자 리스트

  /**
   * 채팅방 고유 ID 생성
   *
   * @implNote ID 형식: "chatroom:user1-user2"
   */
  public static String generateChatRoomId(String senderTsid, String receiverTsid) {

    // 참여자가 없을 경우, 에러 반환
    if (senderTsid == null || receiverTsid == null) {
      throw new IllegalStateException("참여자가 없으면 ID를 생성할 수 없습니다.");
    }

    return "chatroom:" + senderTsid + "-" + receiverTsid;
  }

  /**
   * 채팅방 고유 ID가 현재 participantTsids 기준으로 유효한지 검사
   *
   * @implNote ID 형식 "chatroom:user_123-user_455"
   * @return 유효하면 true, 그렇지 않으면 false
   * @throws IllegalStateException Id 또는 참여자가 없을 경우
   */
  public boolean validateChatRoomId() {

    // Id가 없거나 참여자가 없을 경우, 에러 반환
    if (id == null || participantTsids.isEmpty()) {
      throw new IllegalStateException("ID 또는 참여자 목록이 비어 있습니다.");
    }

    String[] participants = id.substring(id.indexOf(":") + 1).split("-");

    return Arrays.stream(participants).allMatch(participantTsids::contains);
  }

  /**
   * 채팅방의 마지막 메시지 정보 변경
   *
   * @param lastMessage 마지막 메시지 정보
   * @throws IllegalArgumentException 마지막 메시지 정보가 존재하지 않을 경우
   */
  public void updateLastMsg(LastMessage lastMessage) {
    if (lastMessage == null) {
      throw new IllegalArgumentException("채팅방의 마지막 메시지 정보가 존재하지 않습니다.");
    }

    this.lastMessage = lastMessage;
  }
}
