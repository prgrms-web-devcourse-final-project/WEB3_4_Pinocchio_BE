package sns.pinocchio.application.chat.service;

import static sns.pinocchio.presentation.chat.exception.ChatErrorCode.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.chat.dto.ChatRequestDto.GenerateChatroom;
import sns.pinocchio.application.chat.dto.ChatResponseDto.*;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomSortType;
import sns.pinocchio.domain.chatroom.ChatRoomStatus;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepositoryCustom;
import sns.pinocchio.infrastructure.shared.util.TsidUtil;
import sns.pinocchio.presentation.chat.exception.ChatErrorCode;
import sns.pinocchio.presentation.chat.exception.ChatException;
import sns.pinocchio.presentation.member.exception.MemberException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomRepositoryCustom chatRoomRepositoryCustom;
  private final MemberService memberService;

  //  // Chat 저장
  //  Chat savedChat =
  //          chatRepository.save(
  //                  Chat.builder()
  //                          .roomId(chatRoom.getId())
  //                          .roomTsid(chatRoom.getTsid())
  //                          .senderId(senderTsid)
  //                          .receiverId(sendMessage.receiverId())
  //                          .content(sendMessage.messageText())
  //                          .status(ChatStatus.SENT)
  //                          .readStatus(false)
  //                          .likeStatus(false)
  //                          .createdAt(sendMessage.sentAt())
  //                          .createdAtForTsid(TsidUtil.createTsid())
  //                          .modifiedAt(sendMessage.sentAt())
  //                          .build());
  //
  //    log.debug("Saved Chat: {}", savedChat);
  //
  //  // ChatRoom 컬렉션 내 마지막 메시지 내용 수정
  //    chatRoom.updateLastMsg(
  //            LastMessage.builder()
  //            .senderId(senderTsid)
  //            .content(sendMessage.messageText())
  //          .createdAt(sendMessage.sentAt())
  //          .readStatus(false)
  //            .status(ChatStatus.SENT)
  //            .build());
  //
  //  // ChatRoom 수정 사항 반영
  //  ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
  //    log.debug("Saved ChatRoom: {}", savedChatRoom);
  //
  //    log.info("Success to send message: ChatRoom Id[{}] {}", chatRoom.getId(), sendMessage);
  //
  //    return SendMessageInfo.builder()
  //            .chatId(savedChatRoom.getId())
  //          .msgId(savedChat.getId())
  //          .senderId(savedChat.getSenderId())
  //          .receiverId(savedChat.getReceiverId())
  //          .messageText(savedChat.getContent())
  //          .isRead(false)
  //        .messageLike(false)
  //        .sentAt(savedChat.getCreatedAt())
  //          .build();

  /**
   * 채팅방 생성
   *
   * @param userDetails 채팅방 생성 요청 유저 정보
   * @param generateChatroom 채팅방 생성 정보
   * @return ChatRoom 채팅방 정보
   * @throws ChatException 채팅방 생성을 요청한 유저 정보가 존재하지 않을 경우 {@link
   *     ChatErrorCode#UNAUTHORIZED_CHAT_USER} 예외 발생
   * @throws ChatException 수신자가 회원이 아닐 경우 {@link ChatErrorCode#CHAT_NOT_FOUND} 예외 발생
   */
  @Transactional
  public ChatroomInfo getOrGenerateChatRoom(
      CustomUserDetails userDetails, GenerateChatroom generateChatroom) {

    // 로그인한 유저 정보가 존재하지 않을 경우, 401에러 반환
    if (userDetails == null || userDetails.getTsid().isEmpty()) {
      log.error("[{}] No authenticated user found.", this.getClass().getName());
      throw new ChatException(UNAUTHORIZED_CHAT_USER);
    }

    String senderTsid = userDetails.getTsid();

    // 수신자가 회원이 아닐 경우, 404에러 반환
    try {
      memberService.findByTsid(generateChatroom.receiverId());

    } catch (MemberException e) {
      log.error("Receiver User {} not found: {}", generateChatroom.receiverId(), e.getMessage());
      throw new ChatException(CHAT_NOT_FOUND);
    }

    List<String> participants = List.of(senderTsid, generateChatroom.receiverId());

    // 채팅방 유무에 따라 조회 or 생성
    ChatRoom savedChatRoom =
        chatRoomRepository
            .findByParticipantTsids(participants)
            .orElseGet(
                () ->
                    chatRoomRepository.save(
                        ChatRoom.builder()
                            .id(
                                ChatRoom.generateChatRoomId(
                                    senderTsid, generateChatroom.receiverId()))
                            .tsid(TsidUtil.createTsid())
                            .participantTsids(participants)
                            .status(ChatRoomStatus.PENDING)
                            .createdAt(Instant.now())
                            .createdAtTsid(TsidUtil.createTsid())
                            .build()));

    log.info("ChatRoom Created: {}", savedChatRoom);

    return ChatroomInfo.fromChatroom(savedChatRoom);
  }

  /**
   * 채팅방 조회
   *
   * @param userDetails 로그인한 유저 정보
   * @param limit 최대 결과 개수
   * @param sortBy 정렬 기준 (latest / oldest)
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return ChatRoomsInfo 유저가 포함된 채팅방 정보
   * @throws ChatException 조회할 유저의 ID가 존재하지 않을 경우 {@link ChatErrorCode#UNAUTHORIZED_CHAT_USER} 예외 발생
   */
  @Transactional
  public ChatRoomsInfo getChatRooms(
      CustomUserDetails userDetails, int limit, String sortBy, String cursor) {

    // 조회할 유저의 ID가 존재하지 않을 경우, 401에러 반환
    if (userDetails == null || userDetails.getTsid().isEmpty()) {
      log.error("userTsid is null. Fail to get ChatRooms.");
      throw new ChatException(UNAUTHORIZED_CHAT_USER);
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
   * @throws ChatException 로그인한 유저 정보가 존재하지 않을 경우 {@link ChatErrorCode#UNAUTHORIZED_CHAT_USER} 예외 발생
   * @throws ChatException 채팅방을 찾을 수 없을 경우 {@link ChatErrorCode#CHATROOM_NOT_FOUND} 예외 발생
   */
  @Transactional
  public ChatMessagesInfo getMessages(
      CustomUserDetails userDetails, String chatId, int limit, String sortBy, String cursor) {

    // 로그인한 유저 정보가 존재하지 않을 경우, 401에러 반환
    if (userDetails == null || userDetails.getTsid().isEmpty()) {
      log.error("No authenticated user found.");
      throw new ChatException(UNAUTHORIZED_CHAT_USER);
    }

    // 채팅방을 찾을 수 없을 경우, 404에러 반환
    if (chatId == null) {
      log.error("chatId is null. Fail to get Chat Messages in ChatRoom.");
      throw new ChatException(CHATROOM_NOT_FOUND);
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
   * @throws ChatException 상대방 유저가 존재하지 않을 경우 {@link ChatErrorCode#CHAT_USER_NOT_FOUND} 예외 발생
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
              .orElseThrow(() -> new ChatException(CHAT_USER_NOT_FOUND));

      try {
        Member targetUser = memberService.findByTsid(targetUserTsid);
        targetUsers.add(targetUser);

      } catch (MemberException e) {
        log.error("Target User [{}] is null. Fail to get ChatRoom:", targetUserTsid, e);
        throw new ChatException(CHAT_USER_NOT_FOUND);
      }
    }

    return targetUsers;
  }
}
