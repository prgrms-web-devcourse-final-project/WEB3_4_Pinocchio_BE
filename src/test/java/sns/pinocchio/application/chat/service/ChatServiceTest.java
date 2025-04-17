package sns.pinocchio.application.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sns.pinocchio.application.chat.dto.ChatRequestDto.GenerateChatroom;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatMessagesInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatRoomsInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatroomInfo;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chat.ChatStatus;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;
import sns.pinocchio.domain.chatroom.ChatRoomStatus;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.notification.Notification;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepositoryCustom;
import sns.pinocchio.infrastructure.websocket.WebSocketHandler;
import sns.pinocchio.presentation.chat.exception.ChatException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @InjectMocks private ChatService chatService;

  @Mock private ChatRoomRepository chatRoomRepository;

  @Mock private ChatRoomRepositoryCustom chatRoomRepositoryCustom;

  @Mock private ChatRepository chatRepository;

  @Mock private WebSocketHandler webSocketHandler;

  @Mock private CustomUserDetails customUserDetails;

  @Mock private MemberService memberService;

  private String testSenderTsid;

  private String testReceiverTsid;

  private ChatRoom mockChatRoom;

  private Chat mockChat;

  private SendMessage mockSendMessage;

  private Member mockMember1;

  private Member mockMember2;

  private Member mockMember3;

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
            .roomId(mockChatRoom.getId())
            .senderId(testSenderTsid)
            .messageText("테스트 메시지 입니다.")
            .build();

    // 메시지 정보
    mockChat =
        Chat.builder()
            .id("msg_1")
            .roomId(mockChatRoom.getId())
            .senderId(testSenderTsid)
            .content(mockSendMessage.messageText())
            .status(ChatStatus.SENT)
            .readStatus(false)
            .likeStatus(false)
            .build();

    // 상대방 유저
    mockMember1 = Member.builder().nickname("테스트 유저456").build();
    ReflectionTestUtils.setField(mockMember1, "profileImageUrl", "https://img.com/user456.png");

    mockMember2 = Member.builder().nickname("테스트 유저789").build();
    ReflectionTestUtils.setField(mockMember2, "profileImageUrl", "https://img.com/user789.png");

    mockMember3 = Member.builder().nickname("테스트 유저999").build();
    ReflectionTestUtils.setField(mockMember3, "profileImageUrl", "https://img.com/user999.png");
  }

  @Test
  @DisplayName("메시지 전송 Success")
  void sendMessageToChatroomTest() {

    // given
    when(chatRoomRepository.findById(mockChatRoom.getId())).thenReturn(Optional.of(mockChatRoom));
    when(chatRepository.save(any())).thenReturn(mockChat);
    when(chatRoomRepository.save(any())).thenReturn(mockChatRoom);
    when(webSocketHandler.sendMsgToChatroom(any(), any())).thenReturn(true);

    // when
    chatService.sendMessageToChatroom(mockSendMessage);

    // then
    verify(chatRepository, times(1)).save(any(Chat.class));
    verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
  }

  @Test
  @DisplayName("메시지 전송 Fail: 요청값에 대한 유효성 체크가 실패했을 경우")
  void sendMessageToChatroomInvalidRequestTest() {

    // given
    String errorMsg = "입력값이 유효하지 않습니다.";

    // when
    ChatException exception =
        assertThrows(ChatException.class, () -> chatService.sendMessageToChatroom(null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }

  //  @Test
  //  @DisplayName("메시지 전송 Fail: 송신자가 회원이 아닐 경우")
  //  void sendMessageToChatroomNoAuthenticatedUserFoundTest() {
  //
  //    // given
  //    String errorMsg = "송신자의 회원 정보를 찾을 수 없습니다.";
  //
  //    // when
  //    ChatException exception =
  //            assertThrows(ChatException.class, () ->
  // chatService.sendMessageToChatroom(mockSendMessage));
  //
  //    // then
  //    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  //  }

  //  @Test
  //  @DisplayName("메시지 전송 Fail: 수신자에게 메시지 전송을 실패했을 경우")
  //  void sendMessageToChatroomFailSendMsgTest() {
  //
  //    // given
  //    String errorMsg = "메시지 전송에 실패했습니다.";
  //
  //
  // when(chatRoomRepository.findByParticipantTsids(any())).thenReturn(Optional.of(mockChatRoom));
  //
  //    // when
  //    ChatException exception =
  //        assertThrows(ChatException.class, () ->
  // chatService.sendMessageToChatroom(mockSendMessage));
  //
  //    // then
  //    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  //  }

  //  @Test
  //  @DisplayName("메시지 전송 Fail: 수신자에게 메시지 알림 전송을 실패했을 경우")
  //  void sendMessageToChatroomFailSendNotificationTest() {
  //
  //    // given
  //    String errorMsg = "메시지 알림 전송에 실패했습니다.";
  //
  //    Member mockMember = mock(Member.class);
  //    Notification mockNotification = Notification.builder().messageAlert(true).build();
  //
  //    when(mockMember.getNotification()).thenReturn(mockNotification);
  //
  // when(chatRoomRepository.findByParticipantTsids(any())).thenReturn(Optional.of(mockChatRoom));
  //    when(webSocketHandler.sendMsgToChatroom(any(), any())).thenReturn(true);
  //    when(webSocketHandler.sendNotificationToUser(any(), any())).thenReturn(false);
  //
  //    // when
  //    ChatException exception =
  //        assertThrows(
  //            ChatException.class,
  //            () -> chatService.sendMessageToChatroom(mockSendMessage));
  //
  //    // then
  //    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  //  }

  @Test
  @DisplayName("채팅방 생성 Success")
  void createNewChatRoomTest() {

    // given
    GenerateChatroom generateChatroom =
        GenerateChatroom.builder().receiverId(testReceiverTsid).build();

    when(customUserDetails.getTsid()).thenReturn(testSenderTsid);
    when(chatRoomRepository.save(any())).thenReturn(mockChatRoom);

    // when
    ChatroomInfo newChatRoom =
        chatService.getOrGenerateChatRoom(customUserDetails, generateChatroom);

    // then
    assertThat(newChatRoom.roomId()).isEqualTo(mockChatRoom.getId());
    assertThat(newChatRoom.participants()).containsAll(mockChatRoom.getParticipantTsids());
  }

  @Test
  @DisplayName("채팅방 리스트 조회 Success: 커서 X")
  void findChatRoomsSuccessTest() {

    // given
    int limit = 2;
    String sortBy = "latest";
    String cursor = null;

    ChatRoom room1 =
        ChatRoom.builder()
            .id("room1")
            .participantTsids(List.of("user_123", "user_456"))
            .createdAt(Instant.parse("2025-03-30T10:00:00Z"))
            .build();

    ChatRoom room2 =
        ChatRoom.builder()
            .id("room2")
            .participantTsids(List.of("user_123", "user_789"))
            .createdAt(Instant.parse("2025-03-29T10:00:00Z"))
            .build();

    ChatRoom extra =
        ChatRoom.builder()
            .id("room3")
            .participantTsids(List.of("user_123", "user_999"))
            .createdAt(Instant.parse("2025-03-28T10:00:00Z"))
            .build();

    // limit+1 만큼 리턴 → hasNext = true
    List<ChatRoom> fakeRooms = List.of(room1, room2, extra);

    when(customUserDetails.getTsid()).thenReturn(testSenderTsid);
    when(memberService.findByTsid("user_456")).thenReturn(mockMember1);
    when(memberService.findByTsid("user_789")).thenReturn(mockMember2);

    given(
            chatRoomRepositoryCustom.findChatRoomsByUserWithCursor(
                eq(testSenderTsid), isNull(), eq(limit + 1), eq(ChatRoomSortType.LATEST)))
        .willReturn(fakeRooms);

    // when
    ChatRoomsInfo chatRooms = chatService.getChatRooms(customUserDetails, limit, sortBy, cursor);

    // then
    assertThat(chatRooms).isNotNull();
    assertThat(chatRooms.getChatrooms()).hasSize(limit); // limit 개수
    assertThat(chatRooms.isHasNext()).isTrue();
    assertThat(chatRooms.getNextCursor()).isEqualTo(room2.getTsid());
  }

  @Test
  @DisplayName("채팅방 리스트 조회 Success: 커서 O")
  void findChatRoomsWithCursorSuccessTest() {

    // given
    String userTsid = "user_123";
    int limit = 3;
    String sortBy = "latest";
    String cursor = "0K9361EDH5BT5";

    ChatRoom room =
        ChatRoom.builder()
            .id("chatroom:user_123-user_999")
            .participantTsids(List.of("user_123", "user_999"))
            .createdAt(Instant.parse("2025-03-27T10:00:00Z"))
            .createdAtTsid("0K936FV2N581W")
            .build();

    when(customUserDetails.getTsid()).thenReturn(userTsid);
    when(memberService.findByTsid("user_999")).thenReturn(mockMember1);

    given(
            chatRoomRepositoryCustom.findChatRoomsByUserWithCursor(
                eq(userTsid), eq(cursor), eq(4), eq(ChatRoomSortType.LATEST)))
        .willReturn(List.of(room));

    // when
    ChatRoomsInfo result = chatService.getChatRooms(customUserDetails, limit, sortBy, cursor);

    // then
    assertThat(result.isHasNext()).isFalse();
    assertThat(result.getChatrooms()).hasSize(1);
    assertThat(result.getNextCursor()).isNull();
  }

  @Test
  @DisplayName("채팅방 리스트 조회 Fail: 등록된 사용자를 찾을 수 없을 경우")
  void findChatRoomsFailNoUserTest() {

    // given
    String errorMsg = "사용자가 인증되지 않았습니다.";

    // when
    ChatException exception =
        assertThrows(ChatException.class, () -> chatService.getChatRooms(null, 1, "latest", null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }

  @Test
  @DisplayName("채팅 메시지 리스트 조회 Success: 커서 X")
  void findChatMessagesSuccessTest() {

    // given
    String chatId = "chatroom:user_123-user_999";
    int limit = 2;
    String cursor = null;
    String sortBy = "latest";
    ChatRoomSortType sortType = ChatRoomSortType.from(sortBy);

    List<Chat> mockChatList = new ArrayList<>();
    for (int idx = 0; idx < 3; idx++) {
      mockChatList.add(
          Chat.builder()
              .id("msg_" + idx)
              .roomId(chatId)
              .senderId(testSenderTsid)
              .content("테스트 메시지 입니다.")
              .createdAt(Instant.parse("2025-03-01T10:00:00Z"))
              .modifiedAt(Instant.parse("2025-03-01T10:00:00Z"))
              .build());
    }

    when(customUserDetails.getTsid()).thenReturn(testSenderTsid);
    when(chatRoomRepositoryCustom.findChatsByChatRoomWithCursor(
            chatId, cursor, limit + 1, sortType))
        .thenReturn(mockChatList);

    // when
    ChatMessagesInfo messages =
        chatService.getMessages(customUserDetails, chatId, limit, sortBy, cursor);

    // then
    assertThat(messages).isNotNull();
    assertThat(messages.getChatMessages()).hasSize(limit); // limit 개수
    assertThat(messages.isHasNext()).isTrue();
    assertThat(messages.getNextCursor()).isEqualTo(mockChatList.getFirst().getCreatedAtForTsid());
  }

  @Test
  @DisplayName("채팅 메시지 리스트 조회 Success: 커서 기반 조회 확인")
  void findChatMessagesWithCursorSuccessTest() {

    // given
    String chatId = "chatroom:user_123-user_999";
    int limit = 1;
    String cursor = "0K93NA26V94F9";
    String sortBy = "latest";
    ChatRoomSortType sortType = ChatRoomSortType.from(sortBy);

    Chat mockChat =
        Chat.builder()
            .id("msg_1")
            .roomId(chatId)
            .senderId("user_123")
            .content("테스트 메시지 입니다.")
            .createdAt(Instant.parse("2025-03-27T10:00:00Z"))
            .createdAtForTsid("0K93NBYD794M8")
            .modifiedAt(Instant.parse("2025-03-28T10:00:00Z"))
            .build();

    when(customUserDetails.getTsid()).thenReturn(testSenderTsid);
    when(chatRoomRepositoryCustom.findChatsByChatRoomWithCursor(
            chatId, cursor, limit + 1, sortType))
        .thenReturn(List.of(mockChat));

    // when
    ChatMessagesInfo messages =
        chatService.getMessages(customUserDetails, chatId, limit, sortBy, cursor);

    // then
    assertThat(messages.isHasNext()).isFalse();
    assertThat(messages.getChatMessages()).hasSize(1);
    assertThat(messages.getNextCursor()).isNull();
  }

  @Test
  @DisplayName("채팅 메시지 리스트 조회 Fail: 등록된 채팅방을 찾을 수 없을 경우")
  void findChatMessagesFailNoChatRoomTest() {

    // given
    String errorMsg = "등록된 채팅방을 찾을 수 없습니다.";

    when(customUserDetails.getTsid()).thenReturn(testSenderTsid);

    // when
    ChatException exception =
        assertThrows(
            ChatException.class,
            () -> chatService.getMessages(customUserDetails, null, 1, "latest", null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }
}
