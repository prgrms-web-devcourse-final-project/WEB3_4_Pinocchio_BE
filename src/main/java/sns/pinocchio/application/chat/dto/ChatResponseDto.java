package sns.pinocchio.application.chat.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sns.pinocchio.domain.chatroom.ChatRoom;
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

    /**
     * ChatRoom entity -> ChatRoomsDetail Dto
     *
     * @param userTsid 유저 TSID
     * @param chatRoom 채팅방 정보
     * @return ChatRoomsDetail 채팅방 세부 정보
     */
    public static ChatRoomsDetail toDetail(String userTsid, ChatRoom chatRoom) {

      // 채팅 대상 확인: 없을 경우 null
      String targetUserTsid =
          chatRoom.getParticipantTsids().stream()
              .filter(id -> !id.equals(userTsid))
              .findFirst()
              .orElse(null);

      // 읽지 않은 메시지 개수 확인: 없으면 0
      int unreadCounts = 0;
      if (chatRoom.getUnreadCounts() != null && chatRoom.getUnreadCounts().containsKey(userTsid)) {
        unreadCounts = chatRoom.getUnreadCounts().get(userTsid);
      }

      // 마지막 메시지 정보 확인: 없으면 null
      String lastMessage = null;
      Instant lastMessageTime = null;
      if (chatRoom.getLastMessage() != null) {
        lastMessage = chatRoom.getLastMessage().getContent();
        lastMessageTime = chatRoom.getLastMessage().getCreatedAt();
      }

      return ChatRoomsDetail.builder()
          .roomId(chatRoom.getId())
          .targetUserId(targetUserTsid)
          .lastMessage(lastMessage)
          .lastMessageTime(lastMessageTime)
          .unreadCounts(unreadCounts)
          .build();
    }
  }
}
