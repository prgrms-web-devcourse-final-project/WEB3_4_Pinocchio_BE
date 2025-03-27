package sns.pinocchio.presentation.notification;

import static sns.pinocchio.shared.response.GlobalApiResponse.success;
import static sns.pinocchio.shared.swagger.ErrorExamples.*;

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
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.application.notification.service.NotificationService;
import sns.pinocchio.shared.response.GlobalApiResponse;
import sns.pinocchio.shared.swagger.ErrorResponseSchema;

@Tag(name = "Notification", description = "알림 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(summary = "알림 설정 변경", description = "현재 로그인된 사용자의 알림 수신 설정을 변경합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "알림 설정 변경 성공",
        content =
            @Content(
                schema = @Schema(implementation = NotificationInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "400",
        description = "요청 값이 유효하지 않음",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        name = "Bad Request",
                        value = NOTIFICATION_UPDATE_BAD_REQUEST_EXAMPLE))),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class))),
    @ApiResponse(
        responseCode = "500",
        description = "서버 오류",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        name = "Internal Server Error",
                        value = NOTIFICATION_INTERNAL_SERVER_ERROR_EXAMPLE)))
  })
  @PutMapping("/settings")
  public ResponseEntity<GlobalApiResponse<NotificationInfo>> updateNotificationSettings(
      @RequestHeader(value = "Authorization") String accessToken,
      @RequestBody UpdateNotifications request) {

    // todo: JWT 토큰 인증 기능 완료 시, 변경 필요
    NotificationInfo updated = notificationService.updateNotifications("mockUser", request);

    return ResponseEntity.ok(success("알림 설정이 성공적으로 업데이트되었습니다.", updated));
  }

  @Operation(summary = "알림 설정 조회", description = "현재 로그인된 사용자의 알림 수신 설정을 조회합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "알림 설정 조회 성공"),
    @ApiResponse(
        responseCode = "400",
        description = "userId 값이 존재하지 않음",
        content =
            @Content(
                schema = @Schema(implementation = ErrorResponseSchema.class),
                mediaType = "application/json",
                examples =
                    @ExampleObject(
                        name = "Bad Request",
                        value = NOTIFICATION_FIND_BAD_REQUEST_EXAMPLE))),
    @ApiResponse(responseCode = "401", description = "인증 실패")
  })
  @GetMapping("/settings")
  public ResponseEntity<GlobalApiResponse<NotificationInfo>> getNotificationSettings(
      @RequestHeader(value = "Authorization") String accessToken) {

    // todo: JWT 토큰 인증 기능 완료 시, 변경 필요
    NotificationInfo info = notificationService.getNotifications("mockUser");

    return ResponseEntity.ok(success("유저의 알림 설정을 조회했습니다.", info));
  }
}
