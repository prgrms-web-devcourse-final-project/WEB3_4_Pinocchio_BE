package sns.pinocchio.application.chat.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.domain.chat.Chat;
import sns.pinocchio.domain.chat.ChatException.ChatBadRequestException;
import sns.pinocchio.domain.chat.ChatException.ChatInternalServerErrorException;
import sns.pinocchio.domain.chat.ChatStatus;
import sns.pinocchio.domain.chatroom.ChatRoom;
import sns.pinocchio.domain.chatroom.ChatRoomStatus;
import sns.pinocchio.domain.chatroom.LastMessage;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRepository;
import sns.pinocchio.infrastructure.persistence.mongodb.ChatRoomRepository;
import sns.pinocchio.infrastructure.shared.util.TsidUtil;
import sns.pinocchio.infrastructure.websocket.WebSocketHandler;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatRepository chatRepository;
  private final WebSocketHandler webSocketHandler;

  /**
   * 해당 채팅방에 메시지 전송
   *
   * @param senderTsid 발신자 TSID
   * @param sendMessage 메시지 전송 정보
   */
  @Transactional
  public void sendMessageToChatroom(String senderTsid, SendMessage sendMessage) {

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
            .findByParticipantsTsidContainingAll(List.of(senderTsid, sendMessage.receiverTsid()))
            .orElseGet(
                () ->
                    createNewChatRoom(
                        senderTsid, sendMessage.receiverTsid(), sendMessage.sentAt()));

    // 수신자에게 메시지 전송 (실패 시, 500에러 반환)
    if (!webSocketHandler.sendMsgToChatroom(chatRoom.getId(), sendMessage.messageText())) {
      log.error("Failed to send Message: {}", sendMessage);
      throw new ChatInternalServerErrorException("메시지 전송에 실패했습니다. 다시 시도해주세요.");
    }

    log.info("Success to send message: {} {}", chatRoom.getId(), sendMessage);

    // todo: Notification 합칠 때 같이 구현 (message 변경 필요)
    // 메시지 알림 설정이 되어 있을 경우, 알림 전송
    if (!webSocketHandler.sendNotificationToUser(sendMessage.receiverTsid(), "새로운 메시지 도착")) {
      log.error("Failed to send Notification: {}", sendMessage);
      throw new ChatInternalServerErrorException("메시지 알림 전송에 실패했습니다.");
    }

    // Chat 저장
    chatRepository.save(
        Chat.builder()
            .roomId(chatRoom.getId())
            .roomTsid(TsidUtil.createTsid())
            .senderId(senderTsid)
            .receiverId(sendMessage.receiverTsid())
            .content(sendMessage.messageText())
            .status(ChatStatus.SENT)
            .readStatus(false)
            .likeStatus(false)
            .createdAt(sendMessage.sentAt())
            .createdAtForTsid(TsidUtil.createTsid())
            .build());

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
    chatRoomRepository.save(chatRoom);
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
}
