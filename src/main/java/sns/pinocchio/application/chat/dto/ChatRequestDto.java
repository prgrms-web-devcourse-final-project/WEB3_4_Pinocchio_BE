package sns.pinocchio.application.chat.dto;

import lombok.Builder;

public class ChatRequestDto {

  @Builder
  public record SendMessage(String roomId, String senderId, String messageText) {

    // 요청값 유효성 체크 (실패 시, 400에러 반환)
    public boolean validateRequest() {
      return roomId != null && senderId != null && messageText != null;
    }
  }

  @Builder
  public record GenerateChatroom(String receiverId) {}
}
