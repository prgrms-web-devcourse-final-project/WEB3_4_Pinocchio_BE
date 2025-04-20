package sns.pinocchio.presentation.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.application.chat.service.ChatService;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

  private final ChatService chatService;

  @MessageMapping("/chat/message")
  public void handleMessage(SendMessage messageInfo) {

    chatService.sendMessageToChatroom(messageInfo);
  }
}
