package sns.pinocchio.application.chat.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.application.chat.dto.ChatResponseDto.*;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chat.ChatException.ChatBadRequestException;
import sns.pinocchio.domain.chat.ChatException.ChatInternalServerErrorException;
import sns.pinocchio.domain.chat.ChatException.ChatNotFoundException;
import sns.pinocchio.domain.chat.ChatException.ChatUnauthorizedException;
import sns.pinocchio.domain.chat.ChatStatus;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;
import sns.pinocchio.domain.chatroom.ChatRoomStatus;
import sns.pinocchio.domain.chatroom.LastMessage;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepositoryCustom;
import sns.pinocchio.infrastructure.shared.util.TsidUtil;
import sns.pinocchio.infrastructure.websocket.WebSocketHandler;
import sns.pinocchio.presentation.member.exception.MemberException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomRepositoryCustom chatRoomRepositoryCustom;
  private final ChatRepository chatRepository;
  private final WebSocketHandler webSocketHandler;
  private final MemberService memberService;

  /**
   * 해당 채팅방에 메시지 전송
   *
   * @param senderDetails 발신자 정보
   * @param sendMessage 메시지 전송 정보
   * @return SendMessageInfo 전송된 메시지 정보
   * @throws ChatUnauthorizedException 사용자가 인증되지 않았을 경우
   * @throws ChatBadRequestException 입력 값이 유효하지 않을 경우
   * @throws ChatInternalServerErrorException 메시지 전송 또는 메시지 알림 전송이 실패했을 경우
   */
  @Transactional
  public SendMessageInfo sendMessageToChatroom(
      CustomUserDetails senderDetails, SendMessage sendMessage) {

    // 로그인한 유저 정보가 존재하지 않을 경우, 401에러 반환
    if (senderDetails == null || senderDetails.getTsid().isEmpty()) {
      log.error("No authenticated user found: {}", sendMessage);
      throw new ChatUnauthorizedException("사용자가 인증되지 않았습니다. 로그인 후 다시 시도해주세요.");
    }

    String senderTsid = senderDetails.getTsid();

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

    Member loginUser = senderDetails.getMember();

    // 메시지 알림 설정 여부에 따라 알림 전송 (실패 시, 500에러 반환)
    if (loginUser != null
        && loginUser.getNotification() != null
        && loginUser.getNotification().isMessageAlert()) {
      if (!webSocketHandler.sendNotificationToUser(sendMessage.receiverId(), "새로운 메시지 도착")) {
        log.error("Failed to send Notification: {}", sendMessage);
        throw new ChatInternalServerErrorException("메시지 알림 전송에 실패했습니다.");
      }

    } else {
      log.info(
          "Message Notifications are not setting. Skipping notification: {}",
          sendMessage.receiverId());
    }

    // Chat 저장
    Chat savedChat =
        chatRepository.save(
            Chat.builder()
                .roomId(chatRoom.getId())
                .roomTsid(chatRoom.getTsid())
                .senderId(senderTsid)
                .receiverId(sendMessage.receiverId())
                .content(sendMessage.messageText())
                .status(ChatStatus.SENT)
                .readStatus(false)
                .likeStatus(false)
                .createdAt(sendMessage.sentAt())
                .createdAtForTsid(TsidUtil.createTsid())
                .modifiedAt(sendMessage.sentAt())
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
            .id(ChatRoom.generateChatRoomId(senderTsid, receiverTsid))
            .tsid(TsidUtil.createTsid())
            .participantTsids(List.of(senderTsid, receiverTsid))
            .status(ChatRoomStatus.PENDING)
            .createdAt(createdAt)
            .createdAtTsid(TsidUtil.createTsid())
            .build();

    ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
    log.info("New ChatRoom Created: {}", savedChatRoom);

    return savedChatRoom;
  }

  /**
   * 채팅방 조회
   *
   * @param userDetails 로그인한 유저 정보
   * @param limit 최대 결과 개수
   * @param sortBy 정렬 기준 (latest / oldest)
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return ChatRoomsInfo 유저가 포함된 채팅방 정보
   * @throws ChatNotFoundException 등록된 사용자를 찾을 수 없을 경우
   */
  @Transactional
  public ChatRoomsInfo getChatRooms(
      CustomUserDetails userDetails, int limit, String sortBy, String cursor) {

    // 조회할 유저의 ID가 존재하지 않을 경우, 400에러 반환
    if (userDetails == null || userDetails.getTsid().isEmpty()) {
      log.error("userTsid is null. Fail to get ChatRooms.");
      throw new ChatNotFoundException("등록된 사용자를 찾을 수 없습니다.");
    }

    String userTsid = userDetails.getTsid();

    // 정렬 방식 설정
    ChatRoomSortType sortType = ChatRoomSortType.from(sortBy);

    // 채팅방 조회 (다음 데이터 판단을 위해 limit + 1)
    List<ChatRoom> chatRooms =
        chatRoomRepositoryCustom.findChatRoomsByUserWithCursor(
            userTsid, cursor, limit + 1, sortType);

    log.info("Found ChatRooms: count {}, data {}", chatRooms.size(), chatRooms);

    // hasNext 판단: 이후 데이터가 존재하지 않으면 false
    boolean hasNext = chatRooms.size() > limit;

    // 실제 응답에 보낼 데이터는 limit 까지만 저장
    List<ChatRoom> sliced = hasNext ? chatRooms.subList(0, limit) : chatRooms;

    // nextCursor 판단: 이후 데이터가 존재하지 않으면 null
    String nextCursor = hasNext ? sliced.getLast().getCreatedAtTsid() : null;

    // 참여자 정보 조회: 존재하지 않으면 404에러
    List<Member> targetUsers = getTargetUsers(userTsid, sliced);

    // 응답 데이터 생성: ChatRoom Entity -> ChatRoomDetail Dto
    List<ChatRoomsDetail> chatroomDetails =
        IntStream.range(0, sliced.size())
            .mapToObj(i -> ChatRoomsDetail.toDetail(userTsid, targetUsers.get(i), sliced.get(i)))
            .toList();

    return new ChatRoomsInfo(nextCursor, hasNext, chatroomDetails);
  }

  /**
   * 채팅방 내 메시지 조회
   *
   * @param userDetails 로그인한 유저 정보
   * @param chatId 채팅방 TSID
   * @param limit 최대 결과 개수
   * @param sortBy 정렬 기준 (latest / oldest)
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return ChatMessagesInfo 채팅방 내 메시지 정보들
   * @throws ChatUnauthorizedException 사용자가 인증되지 않았을 경우
   * @throws ChatNotFoundException 등록된 채팅방을 찾을 수 없을 경우
   */
  @Transactional
  public ChatMessagesInfo getMessages(
      CustomUserDetails userDetails, String chatId, int limit, String sortBy, String cursor) {

    // 로그인한 유저 정보가 존재하지 않을 경우, 401에러 반환
    if (userDetails == null || userDetails.getTsid().isEmpty()) {
      log.error("No authenticated user found.");
      throw new ChatUnauthorizedException("사용자가 인증되지 않았습니다. 로그인 후 다시 시도해주세요.");
    }

    // 채팅방을 찾을 수 없을 경우, 404에러 반환
    if (chatId == null) {
      log.error("chatId is null. Fail to get Chat Messages in ChatRoom.");
      throw new ChatNotFoundException("등록된 채팅방을 찾을 수 없습니다.");
    }

    // 정렬 방식 설정
    ChatRoomSortType sortType = ChatRoomSortType.from(sortBy);

    // 채팅방 조회 (다음 데이터 판단을 위해 limit + 1)
    List<Chat> chats =
        chatRoomRepositoryCustom.findChatsByChatRoomWithCursor(chatId, cursor, limit + 1, sortType);

    log.info("Found Chats: count {}, data {}", chats.size(), chats);

    // hasNext 판단: 이후 데이터가 존재하지 않으면 false
    boolean hasNext = chats.size() > limit;

    // 실제 응답에 보낼 데이터는 limit 까지만 저장
    List<Chat> sliced = hasNext ? chats.subList(0, limit) : chats;

    // nextCursor 판단: 이후 데이터가 존재하지 않으면 null
    String nextCursor = hasNext ? sliced.getLast().getCreatedAtForTsid() : null;

    // 응답 데이터 생성: Chat Entity -> ChatMessageDetail Dto
    List<ChatMessageDetail> chatMessageDetail =
        sliced.stream().map(ChatMessageDetail::toDetail).toList();

    return new ChatMessagesInfo(nextCursor, hasNext, chatId, chatMessageDetail);
  }

  /**
   * 채팅방 참여자 중, 현재 로그인한 유저를 제외한 상대방 유저 리스트 조회
   *
   * @param userTsid 로그인한 유저 TSID
   * @param chatRooms 채팅방 리스트
   * @return List<Member> 상대방 유저 리스트
   * @throws ChatNotFoundException 등록된 유저를 찾을 수 없을 경우
   */
  @Transactional(readOnly = true)
  public List<Member> getTargetUsers(String userTsid, List<ChatRoom> chatRooms) {
    List<Member> targetUsers = new ArrayList<>();

    for (ChatRoom chatroom : chatRooms) {

      // 채팅방 참여자 내에서 로그인한 유저를 제외한 상대방 유저 찾기
      String targetUserTsid =
          chatroom.getParticipantTsids().stream()
              .filter(user -> !user.equals(userTsid))
              .findFirst()
              .orElseThrow(() -> new ChatNotFoundException("상대방 사용자의 정보가 존재하지 않습니다."));

      try {
        Member targetUser = memberService.findByTsid(targetUserTsid);
        targetUsers.add(targetUser);

      } catch (MemberException e) {
        log.error("Target User [{}] is null. Fail to get ChatRoom:", targetUserTsid, e);
        throw new ChatNotFoundException("상대방 사용자의 정보가 존재하지 않습니다.");
      }
    }

    return targetUsers;
  }
}
