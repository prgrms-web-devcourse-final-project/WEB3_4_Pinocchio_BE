package sns.pinocchio.application.chat.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatRoomsDetail;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatRoomsInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.SendMessageInfo;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chat.ChatException.ChatBadRequestException;
import sns.pinocchio.domain.chat.ChatException.ChatInternalServerErrorException;
import sns.pinocchio.domain.chat.ChatException.ChatNotFoundException;
import sns.pinocchio.domain.chat.ChatStatus;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;
import sns.pinocchio.domain.chatroom.ChatRoomStatus;
import sns.pinocchio.domain.chatroom.LastMessage;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepositoryCustom;
import sns.pinocchio.infrastructure.shared.util.TsidUtil;
import sns.pinocchio.infrastructure.websocket.WebSocketHandler;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomRepositoryCustom chatRoomRepositoryCustom;
  private final ChatRepository chatRepository;
  private final WebSocketHandler webSocketHandler;

  /**
   * 해당 채팅방에 메시지 전송
   *
   * @param senderTsid 발신자 TSID
   * @param sendMessage 메시지 전송 정보
   */
  @Transactional
  public SendMessageInfo sendMessageToChatroom(String senderTsid, SendMessage sendMessage) {

    // 송신자 ID가 존재하지 않을 경우, 400에러 반환
    if (senderTsid == null) {
      log.error("Sender ID is null: {}", sendMessage);
      throw new ChatBadRequestException("입력값이 유효하지 않습니다.");
    }

    // 요청 값에 대한 유효성 체크가 실패 했을 경우, 400에러 반환
    if (sendMessage == null || !sendMessage.validateRequest()) {
      log.error("The requested send message info are invalid: {}", sendMessage);
      throw new ChatBadRequestException("입력값이 유효하지 않습니다.");
    }

    // sender와 receiver가 속한 RoomId를 조회 (존재하지 않으면, 새로 생성)
    ChatRoom chatRoom =
        chatRoomRepository
            .findByParticipantTsids(List.of(senderTsid, sendMessage.receiverId()))
            .orElseGet(
                () ->
                    createNewChatRoom(senderTsid, sendMessage.receiverId(), sendMessage.sentAt()));

    // 수신자에게 메시지 전송 (실패 시, 500에러 반환)
    if (!webSocketHandler.sendMsgToChatroom(chatRoom.getId(), sendMessage.messageText())) {
      log.error("Failed to send Message: {}", sendMessage);
      throw new ChatInternalServerErrorException("메시지 전송에 실패했습니다. 다시 시도해주세요.");
    }

    // todo: Notification 합칠 때 같이 구현 (message 변경 필요)
    // 메시지 알림 설정이 되어 있을 경우, 알림 전송
    if (!webSocketHandler.sendNotificationToUser(sendMessage.receiverId(), "새로운 메시지 도착")) {
      log.error("Failed to send Notification: {}", sendMessage);
      throw new ChatInternalServerErrorException("메시지 알림 전송에 실패했습니다.");
    }

    // Chat 저장
    Chat savedChat =
        chatRepository.save(
            Chat.builder()
                .roomId(chatRoom.getId())
                .roomTsid(TsidUtil.createTsid())
                .senderId(senderTsid)
                .receiverId(sendMessage.receiverId())
                .content(sendMessage.messageText())
                .status(ChatStatus.SENT)
                .readStatus(false)
                .likeStatus(false)
                .createdAt(sendMessage.sentAt())
                .createdAtForTsid(TsidUtil.createTsid())
                .build());

    log.debug("Saved Chat: {}", savedChat);

    // ChatRoom 컬렉션 내 마지막 메시지 내용 수정
    chatRoom.updateLastMsg(
        LastMessage.builder()
            .senderId(senderTsid)
            .content(sendMessage.messageText())
            .createdAt(sendMessage.sentAt())
            .readStatus(false)
            .status(ChatStatus.SENT)
            .build());

    // ChatRoom 수정 사항 반영
    ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
    log.debug("Saved ChatRoom: {}", savedChatRoom);

    log.info("Success to send message: ChatRoom Id[{}] {}", chatRoom.getId(), sendMessage);

    return SendMessageInfo.builder()
        .chatId(savedChatRoom.getId())
        .msgId(savedChat.getId())
        .senderId(savedChat.getSenderId())
        .receiverId(savedChat.getReceiverId())
        .messageText(savedChat.getContent())
        .isRead(false)
        .messageLike(false)
        .sentAt(savedChat.getCreatedAt())
        .build();
  }

  /**
   * 채팅방 생성
   *
   * @param senderTsid 발신자 TSID
   * @param receiverTsid 송신자 TSID
   * @param createdAt 채팅방 생성 날짜
   * @return ChatRoom 채팅방 정보
   */
  @Transactional
  public ChatRoom createNewChatRoom(String senderTsid, String receiverTsid, Instant createdAt) {
    ChatRoom newChatRoom =
        ChatRoom.builder()
            .participantTsids(List.of(senderTsid, receiverTsid))
            .status(ChatRoomStatus.PENDING)
            .createdAt(createdAt)
            .build();

    // 채팅방 ID 생성
    newChatRoom.generateChatRoomId();

    ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);

    // DB에 저장하는 도중 에러가 발생했을 경우, 500에러 반환
    if (savedChatRoom == null) {
      log.error("Failed to save ChatRoom for sender: {}, receiver: {}", senderTsid, receiverTsid);
      throw new ChatInternalServerErrorException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    log.info("New ChatRoom Created: {}", savedChatRoom);
    return savedChatRoom;
  }

  /**
   * 채팅방 조회
   *
   * @param userTsid 조회할 유저의 Tsid
   * @param limit 최대 결과 개수
   * @param sortBy 정렬 기준 (latest / oldest)
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return ChatRoomsInfo 유저가 포함된 채팅방 정보
   */
  @Transactional
  public ChatRoomsInfo getChatRooms(String userTsid, int limit, String sortBy, String cursor) {

    if (userTsid == null) {
      log.error("userTsid is null. Fail to get ChatRooms.");
      throw new ChatNotFoundException("등록된 사용자를 찾을 수 없습니다.");
    }

    Instant parsedCursor = null;

    // 커서가 존재하면 Instant 포맷으로 변경
    if (cursor != null) {
      parsedCursor = Instant.parse(cursor);
    }

    // 정렬 방식 설정
    ChatRoomSortType sortType = ChatRoomSortType.from(sortBy);

    // 채팅방 조회 (다음 데이터 판단을 위해 limit + 1)
    List<ChatRoom> chatrooms =
        chatRoomRepositoryCustom.findChatRoomsByUserWithCursor(
            userTsid, parsedCursor, limit + 1, sortType);

    log.info("Found ChatRooms: count {}, data {}", chatrooms.size(), chatrooms);

    // hasNext 판단: 이후 데이터가 존재하지 않으면 false
    boolean hasNext = chatrooms.size() > limit;

    // 실제 응답에 보낼 데이터는 limit까지만 저장
    List<ChatRoom> sliced = hasNext ? chatrooms.subList(0, limit) : chatrooms;

    List<ChatRoomsDetail> chatroomDetails =
        sliced.stream().map(chatRoom -> toDetail(userTsid, chatRoom)).toList();

    // nextCursor 판단: 이후 데이터가 존재하지 않으면 null
    String nextCursor = hasNext ? sliced.getLast().getCreatedAt().toString() : null;

    return new ChatRoomsInfo(nextCursor, hasNext, chatroomDetails);
  }

  /**
   * ChatRoom entity -> ChatRoomsDetail Dto
   *
   * @param userTsid 유저 TSID
   * @param chatRoom 채팅방 정보
   * @return ChatRoomsDetail 채팅방 세부 정보
   */
  private ChatRoomsDetail toDetail(String userTsid, ChatRoom chatRoom) {

    // 채팅 대상 확인: 없을 경우 null
    String targetUserTsid =
        chatRoom.getParticipantTsids().stream()
            .filter(id -> !id.equals(userTsid))
            .findFirst()
            .orElseGet(null);

    // 읽지 않은 메시지 개수 확인: 없으면 0
    int unreadCounts = 0;

    if (chatRoom.getUnreadCounts() != null && chatRoom.getUnreadCounts().containsKey(userTsid)) {
      unreadCounts = chatRoom.getUnreadCounts().get(userTsid);
    }

    return ChatRoomsDetail.builder()
        .roomId(chatRoom.getId())
        .targetUserId(targetUserTsid)
        .lastMessage(chatRoom.getLastMessage().getContent())
        .lastMessageTime(chatRoom.getLastMessage().getCreatedAt())
        .unreadCounts(unreadCounts)
        .build();
  }
}
