package sns.pinocchio.domain.chat;

import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "chats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {

  @Id private String id; // 메시지 ID

  @Indexed private String roomId; // 채팅방 ID

  @Indexed private String roomTsid; // 채팅방 TSID

  private String senderId; // 송신자 ID

  private String receiverId; // 발신자 ID

  private String type; // 메시지 유형 (TEXT)

  private String content; // 메시지 내용

  private boolean readStatus; // 메시지 읽음 상태

  private boolean likeStatus; // 메시지 좋아요 상태

  private ChatStatus status; // 메시지 전송 상태 (SENT | DELIVERED | READ)

  @Indexed private Instant createdAt; // 메시지 생성 날짜

  @Indexed private String createdAtForTsid; // TSID용 메시지 생성 날짜

  private Instant modifiedAt; // 메시지 수정 날짜
}
