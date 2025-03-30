package sns.pinocchio.application.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.application.chat.dto.ChatResponseDto.SendMessageInfo;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chat.ChatException.ChatBadRequestException;
import sns.pinocchio.domain.chat.ChatException.ChatInternalServerErrorException;
import sns.pinocchio.domain.chat.ChatStatus;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomStatus;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepository;
import sns.pinocchio.infrastructure.websocket.WebSocketHandler;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks private ChatService chatService;

  @Mock private ChatRoomRepository chatRoomRepository;

  @Mock private ChatRepository chatRepository;

  @Mock private WebSocketHandler webSocketHandler;

  private String testSenderTsid;

  private String testReceiverTsid;

  private ChatRoom mockChatRoom;

  private Chat mockChat;

  private SendMessage mockSendMessage;

  @BeforeEach
  void setUp() {

    // 송신자 & 수신자 정보
    testSenderTsid = "user_123";

    testReceiverTsid = "user_456";

    // 채팅방 정보
    mockChatRoom =
        ChatRoom.builder()
            .id("chatroom:%s-%s".formatted(testSenderTsid, testReceiverTsid))
            .participantTsids(List.of(testSenderTsid, testReceiverTsid))
            .createdAt(Instant.now())
            .status(ChatRoomStatus.PENDING)
            .build();

    // 요청 전송 메시지 정보
    mockSendMessage =
        SendMessage.builder()
            .receiverId("user_456")
            .messageText("테스트 메시지 입니다.")
            .sentAt(Instant.now())
            .build();

    // 메시지 정보
    mockChat =
        Chat.builder()
            .id("msg_1")
            .roomId(mockChatRoom.getId())
            .senderId(testSenderTsid)
            .receiverId(testReceiverTsid)
            .content(mockSendMessage.messageText())
            .status(ChatStatus.SENT)
            .readStatus(false)
            .likeStatus(false)
            .createdAt(mockSendMessage.sentAt())
            .build();
  }

  @Test
  @DisplayName("메시지 전송 Success")
  void sendMessageToChatroomTest() {

    // given
    when(chatRoomRepository.findByParticipantTsids(any())).thenReturn(Optional.of(mockChatRoom));
    when(webSocketHandler.sendMsgToChatroom(any(), any())).thenReturn(true);
    when(webSocketHandler.sendNotificationToUser(any(), any())).thenReturn(true);
    when(chatRepository.save(any())).thenReturn(mockChat);
    when(chatRoomRepository.save(any())).thenReturn(mockChatRoom);

    // when
    SendMessageInfo messageInfo =
        chatService.sendMessageToChatroom(testSenderTsid, mockSendMessage);

    // then
    verify(chatRepository, times(1)).save(any(Chat.class));
    verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));

    assertThat(messageInfo.chatId()).isEqualTo(mockChatRoom.getId());
    assertThat(messageInfo.msgId()).isEqualTo(mockChat.getId());
    assertThat(messageInfo.senderId()).isEqualTo(mockChat.getSenderId());
    assertThat(messageInfo.receiverId()).isEqualTo(mockChat.getReceiverId());
    assertThat(messageInfo.messageText()).isEqualTo(mockChat.getContent());
    assertThat(messageInfo.isRead()).isEqualTo(mockChat.isReadStatus());
    assertThat(messageInfo.messageLike()).isEqualTo(mockChat.isLikeStatus());
    assertThat(messageInfo.sentAt()).isEqualTo(mockChat.getCreatedAt());
  }

  @Test
  @DisplayName("메시지 전송 Fail: 송신자 ID가 존재하지 않을 경우")
  void sendMessageToChatroomNoSenderTest() {

    // given
    String errorMsg = "입력값이 유효하지 않습니다.";

    // when
    ChatBadRequestException exception =
        assertThrows(
            ChatBadRequestException.class,
            () -> chatService.sendMessageToChatroom(null, mockSendMessage));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }

  @Test
  @DisplayName("메시지 전송 Fail: 요청 받은 메시지 정보가 존재하지 않을 경우")
  void sendMessageToChatroomNoMessageInfoTest() {

    // given
    String errorMsg = "입력값이 유효하지 않습니다.";

    // when
    ChatBadRequestException exception =
        assertThrows(
            ChatBadRequestException.class,
            () -> chatService.sendMessageToChatroom(testSenderTsid, null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }

  @Test
  @DisplayName("메시지 전송 Fail: 수신자에게 메시지 전송을 실패했을 경우")
  void sendMessageToChatroomFailSendMsgTest() {

    // given
    String errorMsg = "메시지 전송에 실패했습니다. 다시 시도해주세요.";

    when(chatRoomRepository.findByParticipantTsids(any())).thenReturn(Optional.of(mockChatRoom));

    // when
    ChatInternalServerErrorException exception =
        assertThrows(
            ChatInternalServerErrorException.class,
            () -> chatService.sendMessageToChatroom(testSenderTsid, mockSendMessage));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }

  @Test
  @DisplayName("메시지 전송 Fail: 수신자에게 메시지 알림 전송을 실패했을 경우")
  void sendMessageToChatroomFailSendNotificationTest() {

    // given
    String errorMsg = "메시지 알림 전송에 실패했습니다.";

    when(chatRoomRepository.findByParticipantTsids(any())).thenReturn(Optional.of(mockChatRoom));
    when(webSocketHandler.sendMsgToChatroom(any(), any())).thenReturn(true);

    // when
    ChatInternalServerErrorException exception =
        assertThrows(
            ChatInternalServerErrorException.class,
            () -> chatService.sendMessageToChatroom(testSenderTsid, mockSendMessage));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }

  @Test
  @DisplayName("채팅방 생성 Success")
  void createNewChatRoomTest() {

    // given
    when(chatRoomRepository.save(any())).thenReturn(mockChatRoom);

    // when
    ChatRoom newChatRoom =
        chatService.createNewChatRoom(
            testSenderTsid, testReceiverTsid, mockChatRoom.getCreatedAt());

    // then
    assertThat(newChatRoom.getParticipantTsids()).containsAll(mockChatRoom.getParticipantTsids());
    assertThat(newChatRoom.getStatus()).isEqualTo(ChatRoomStatus.PENDING);
  }

  @Test
  @DisplayName("채팅방 생성 Fail: 채팅방 정보를 DB에 저장하는 도중 실패했을 경우")
  void createNewChatRoomFailSavedTest() {

    // given
    String errorMsg = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";

    when(chatRoomRepository.save(any())).thenReturn(null);

    // when
    ChatInternalServerErrorException exception =
        assertThrows(
            ChatInternalServerErrorException.class,
            () ->
                chatService.createNewChatRoom(
                    testSenderTsid, testReceiverTsid, mockChatRoom.getCreatedAt()));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }
}
