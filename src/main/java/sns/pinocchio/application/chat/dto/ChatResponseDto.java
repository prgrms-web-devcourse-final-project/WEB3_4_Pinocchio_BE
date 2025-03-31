package sns.pinocchio.application.chat.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sns.pinocchio.infrastructure.shared.response.GlobalCursorPageResponse;

public class ChatResponseDto {

  @Builder
  public record SendMessageInfo(
      String chatId,
      String msgId,
      String senderId,
      String receiverId,
      String messageText,
      boolean isRead,
      boolean messageLike,
      Instant sentAt) {}

  @Getter
  public static class ChatRoomsInfo extends GlobalCursorPageResponse {

    private final List<ChatRoomsDetail> chatrooms;

    public ChatRoomsInfo(String nextCursor, boolean hasNext, List<ChatRoomsDetail> chatrooms) {
      super(nextCursor, hasNext);
      this.chatrooms = chatrooms;
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class ChatRoomsDetail {

    private String roomId;

    private String targetUserId;

    private String lastMessage;

    private Instant lastMessageTime;

    private int unreadCounts;
  }
}
