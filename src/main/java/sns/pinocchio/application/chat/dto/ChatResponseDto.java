package sns.pinocchio.application.chat.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.member.Member;
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

    private String roomIdForSearch;

    private String createdAt;

    private String createdAtForSearch;

    private String targetUserId;

    private String targetUserNickName;

    private String targetUserProfileImageUrl;

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
    public static ChatRoomsDetail toDetail(String userTsid, Member targetUser, ChatRoom chatRoom) {

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
          .roomIdForSearch(chatRoom.getTsid())
          .createdAt(chatRoom.getCreatedAt().toString())
          .createdAtForSearch(chatRoom.getCreatedAtTsid())
          .targetUserId(targetUserTsid)
          .targetUserNickName(targetUser.getNickname())
          .targetUserProfileImageUrl(targetUser.getProfileImageUrl())
          .lastMessage(lastMessage)
          .lastMessageTime(lastMessageTime)
          .unreadCounts(unreadCounts)
          .build();
    }
  }

  @Getter
  public static class ChatMessagesInfo extends GlobalCursorPageResponse {

    private final String chatId;
    private final List<ChatMessageDetail> chatMessages;

    public ChatMessagesInfo(
        String nextCursor, boolean hasNext, String chatId, List<ChatMessageDetail> chatMessages) {
      super(nextCursor, hasNext);
      this.chatId = chatId;
      this.chatMessages = chatMessages;
    }
  }

  @Getter
  @Builder
  @AllArgsConstructor
  public static class ChatMessageDetail {

    private String msgId;

    private String senderId;

    private String receiverId;

    private String content;

    private boolean readStatus;

    private boolean likeStatus;

    private String createdAt;

    private String createdAtForCursor;

    private String modifiedAt;

    /**
     * Chat entity -> ChatMessageDetail Dto
     *
     * @param chatMessage 채팅방 메시지 정보
     * @return ChatMessageDetail 채팅방 메시지 세부 정보
     */
    public static ChatMessageDetail toDetail(Chat chatMessage) {

      return ChatMessageDetail.builder()
          .msgId(chatMessage.getId())
          .senderId(chatMessage.getSenderId())
          .receiverId(chatMessage.getReceiverId())
          .content(chatMessage.getContent())
          .readStatus(chatMessage.isReadStatus())
          .likeStatus(chatMessage.isLikeStatus())
          .createdAt(chatMessage.getCreatedAt().toString())
          .createdAtForCursor(chatMessage.getCreatedAtForTsid())
          .modifiedAt(chatMessage.getModifiedAt().toString())
          .build();
    }
  }

  @Builder
  public record ChatroomInfo(
      String roomId, String roomIdForSearch, String createdAt, List<String> participants) {

    public static ChatroomInfo fromChatroom(ChatRoom chatroom) {
      return ChatroomInfo.builder()
          .roomId(chatroom.getId())
          .roomIdForSearch(chatroom.getTsid())
          .createdAt(chatroom.getCreatedAt().toString())
          .participants(chatroom.getParticipantTsids())
          .build();
    }
  }
}
