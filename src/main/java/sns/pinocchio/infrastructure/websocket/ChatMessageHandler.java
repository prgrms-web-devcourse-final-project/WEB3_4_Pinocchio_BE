package sns.pinocchio.infrastructure.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatMessageHandler {

  private final SimpMessagingTemplate messagingTemplate;

  /**
   * 해당 채팅방에 메시지를 전송하는 기능
   *
   * @param roomId 채팅방 ID
   * @param message 메시지 내용
   * @exception MessageConversionException 메시지 내용 JSON 직렬화 실패했을 경우
   * @exception MessagingException 메시지 채널이 닫혔거나 비정상일 경우
   */
  public void sendMsgToChatroom(String roomId, String message) {

    try {
      messagingTemplate.convertAndSend("/topic/chatroom/" + roomId, message);

    } catch (MessageConversionException e) {
      log.error("메시지 직렬화 실패: {}", e.getMessage());

    } catch (MessagingException e) {
      log.error("WebSocket 메시지 전송 실패: {}", e.getMessage());
    }
  }
}
