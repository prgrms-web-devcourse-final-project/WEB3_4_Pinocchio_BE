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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.chat.dto.ChatRequestDto.GenerateChatroom;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatMessagesInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatRoomsInfo;
import sns.pinocchio.application.chat.dto.ChatResponseDto.ChatroomInfo;
import sns.pinocchio.application.chat.service.ChatService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.infrastructure.shared.response.GlobalApiResponse;
import sns.pinocchio.infrastructure.shared.swagger.ErrorResponseSchema;

@Tag(name = "채팅", description = "채팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

  private final ChatService chatService;

  @Operation(summary = "채팅방 생성", description = "송신자와 수신자가 포함되어 있는 채팅방을 찾고 존재하지 않으면 새로 생성합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "채팅방 생성 성공",
        content =
            @Content(
                schema = @Schema(implementation = ChatroomInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples = {
                  @ExampleObject(summary = "유효하지 않는 인증 정보입니다.", value = UNAUTHORIZED_EXAMPLE)
                })),
    @ApiResponse(
        responseCode = "404",
        description = "송수신자 없음",
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
  @PostMapping("/room")
  public ResponseEntity<GlobalApiResponse<ChatroomInfo>> generateChatroom(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody GenerateChatroom generateChatroom) {

    ChatroomInfo chatroomInfo = chatService.getOrGenerateChatRoom(userDetails, generateChatroom);

    return ResponseEntity.ok(GlobalApiResponse.success("채팅방을 생성했습니다.", chatroomInfo));
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
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples = {
                  @ExampleObject(summary = "유효하지 않는 인증 정보입니다.", value = UNAUTHORIZED_EXAMPLE)
                })),
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
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "limit", defaultValue = "9", required = false) int limit,
      @RequestParam(name = "sortBy", defaultValue = "latest", required = false) String sortBy,
      @RequestParam(name = "cursor", required = false) String cursor) {

    ChatRoomsInfo chatRoomsInfo = chatService.getChatRooms(userDetails, limit, sortBy, cursor);

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
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples = {
                  @ExampleObject(summary = "유효하지 않는 인증 정보입니다.", value = UNAUTHORIZED_EXAMPLE)
                })),
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
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "limit", defaultValue = "9", required = false) int limit,
      @RequestParam(name = "sortBy", defaultValue = "latest", required = false) String sortBy,
      @RequestParam(name = "cursor", required = false) String cursor) {

    ChatMessagesInfo messageInfo =
        chatService.getMessages(userDetails, chatId, limit, sortBy, cursor);

    return ResponseEntity.ok(GlobalApiResponse.success("채팅방의 채팅 메시지 목록을 조회했습니다.", messageInfo));
  }
}
