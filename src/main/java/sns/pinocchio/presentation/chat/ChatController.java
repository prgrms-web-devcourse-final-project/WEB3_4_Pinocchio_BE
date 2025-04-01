package sns.pinocchio.presentation.chat;

import static sns.pinocchio.infrastructure.shared.swagger.ErrorExamples.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.chat.dto.ChatRequestDto.SendMessage;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatMessagesInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatRoomsInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.SendMessageInfo;
import sns.pinocchio.application.chat.service.ChatService;
import sns.pinocchio.infrastructure.shared.response.GlobalApiResponse;
import sns.pinocchio.infrastructure.shared.swagger.ErrorResponseSchema;

@Tag(name = "채팅", description = "채팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

  private final ChatService chatService;

  @Operation(summary = "메시지 전송", description = "발신자가 수신자에게 메시지를 전송합니다. (알림 설정 시, 수신자에게 메시지 도착 알림)")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "메시지 전송 성공",
        content =
            @Content(
                schema = @Schema(implementation = SendMessageInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "400",
        description = "입력 값이 유효하지 않음",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples =
                    @ExampleObject(name = "Bad Request", value = SEND_CHAT_BAD_REQUEST_EXAMPLE))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))),
    @ApiResponse(
        responseCode = "500",
        description = "메시지 전송 실패 또는 알림 전송 실패",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      name = "Message Send Failure",
                      summary = "메시지 전송 실패",
                      value = SEND_CHAT_INTERNAL_SERVER_ERROR_EXAMPLE),
                  @ExampleObject(
                      name = "Notification Send Failure",
                      summary = "알림 전송 실패",
                      value = SEND_CHAT_NOTIFICATION_INTERNAL_SERVER_ERROR_EXAMPLE)
                }))
  })
  @PostMapping("/{senderId}")
  public ResponseEntity<GlobalApiResponse<SendMessageInfo>> sendMessage(
      @RequestHeader(value = "Authorization") String accessToken,
      @PathVariable(name = "senderId") String senderTsid,
      @RequestBody SendMessage messageInfo) {

    // todo: JWT 토큰 인증 기능 완료 시, 추가 필요

    SendMessageInfo sendMessageInfo = chatService.sendMessageToChatroom(senderTsid, messageInfo);

    return ResponseEntity.ok(GlobalApiResponse.success("메시지가 성공적으로 전송되었습니다.", sendMessageInfo));
  }

  @Operation(summary = "채팅방 리스트 조회", description = "로그인한 유저의 채팅방 리스트를 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "유저의 채팅방 목록을 조회했습니다.",
        content =
            @Content(
                schema = @Schema(implementation = ChatRoomsInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))),
    @ApiResponse(
        responseCode = "404",
        description = "사용자 없음",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      summary = "등록된 사용자를 찾을 수 없습니다.",
                      value = FIND_CHAT_ROOM_NOT_FOUND_EXAMPLE)
                }))
  })
  @GetMapping("/list")
  public ResponseEntity<GlobalApiResponse<ChatRoomsInfo>> getChatRooms(
      @RequestHeader(value = "Authorization") String accessToken,
      @RequestParam(name = "limit", defaultValue = "9", required = false) int limit,
      @RequestParam(name = "sortBy", defaultValue = "latest", required = false) String sortBy,
      @RequestParam(name = "cursor", required = false) String cursor) {

    // TODO: 코드 병합 시, 해당 메서드는 UserController로 이동 필요
    // TODO: JWT 토큰 인증 기능 완료 시, 추가 필요

    ChatRoomsInfo chatRoomsInfo = chatService.getChatRooms("mockUser", limit, sortBy, cursor);

    return ResponseEntity.ok(GlobalApiResponse.success("유저의 채팅방 목록을 조회했습니다.", chatRoomsInfo));
  }

  @Operation(summary = "채팅 메시지 리스트 조회", description = "해당 채팅방의 채팅 메시지 목록을 조회합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "채팅방의 채팅 메시지 목록을 조회했습니다.",
        content =
            @Content(
                schema = @Schema(implementation = ChatRoomsInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))),
    @ApiResponse(
        responseCode = "404",
        description = "채팅방 없음",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples = {
                  @ExampleObject(
                      summary = "등록된 사용자를 찾을 수 없습니다.",
                      value = FIND_CHAT_MESSAGE_NOT_FOUND_EXAMPLE)
                }))
  })
  @GetMapping("/{chatId}/messages")
  public ResponseEntity<GlobalApiResponse<ChatMessagesInfo>> getMessages(
      @PathVariable(name = "chatId") String chatId,
      @RequestHeader(value = "Authorization") String accessToken,
      @RequestParam(name = "limit", defaultValue = "9", required = false) int limit,
      @RequestParam(name = "sortBy", defaultValue = "latest", required = false) String sortBy,
      @RequestParam(name = "cursor", required = false) String cursor) {

    // TODO: JWT 토큰 인증 기능 완료 시, 추가 필요

    ChatMessagesInfo messageInfo = chatService.getMessages(chatId, limit, sortBy, cursor);

    return ResponseEntity.ok(GlobalApiResponse.success("채팅방의 채팅 메시지 목록을 조회했습니다.", messageInfo));
  }
}
