package sns.pinocchio.application.chat.dto;

import lombok.Builder;

import java.time.Instant;

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
}
