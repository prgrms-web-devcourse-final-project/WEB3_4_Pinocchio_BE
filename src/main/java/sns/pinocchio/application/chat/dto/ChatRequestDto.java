package sns.pinocchio.application.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.Builder;

public class ChatRequestDto {

  @Builder
  public record SendMessage(
      String receiverId,
      String messageText,
      @JsonFormat(shape = JsonFormat.Shape.STRING) Instant sentAt) {

    // 요청값 유효성 체크 (실패 시, 400에러 반환)
    public boolean validateRequest() {
      return receiverId != null && messageText != null && sentAt != null;
    }
  }
}
