package sns.pinocchio.application.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.domain.notification.Notification;
import sns.pinocchio.domain.notification.NotificationException.NotificationBadRequestException;
import sns.pinocchio.domain.notification.NotificationException.NotificationInternalServerErrorException;
import sns.pinocchio.infrastructure.persistence.mysql.NotificationRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  /**
   * 입력받은 사용자의 알림 설정을 업데이트
   *
   * @implNote 현재는 임시로 userId를 하드코딩하고 있으며, 실제 서비스 적용 시 JWT 인증을 통해 사용자 ID를 추출하도록 변경 필요
   * @param updateNotifications 변경할 알림 설정 값들을 담은 DTO
   * @return NotificationInfo 변경된 알림 설정 정보를 담은 응답 DTO
   */
  @Transactional
  public NotificationInfo updateNotifications(
      String userId, UpdateNotifications updateNotifications) {

    // 설정 정보가 존재하지 않을 경우, 400에러 반환
    if (!updateNotifications.checkNotifications()) {
      log.error("The requested notification settings are invalid: {}", updateNotifications);
      throw new NotificationBadRequestException("입력값이 유효하지 않습니다.");
    }

    // 회원의 알림 설정 조회
    Notification notification =
        notificationRepository
            .findByUserId(userId)
            .orElse(
                Notification.builder()
                    .userId(userId)
                    .messageAlert(updateNotifications.message())
                    .likeAlert(updateNotifications.like())
                    .commentAlert(updateNotifications.comment())
                    .followAlert(updateNotifications.follow())
                    .mentionAlert(updateNotifications.mention())
                    .build());

    // 요청받은 알림 변경 사항들로 알림 수정
    notification.update(updateNotifications);

    // 수정된 알림 설정을 기반으로 DB 수정
    Notification updated = notificationRepository.save(notification);

    // DB에 수정하는 도중 에러가 발생했을 경우, 500에러 반환
    if (updated == null) {
      log.error("Failed to save notification: {}", notification);
      throw new NotificationInternalServerErrorException("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    log.info("Notification settings updated: {}", updated);

    return NotificationInfo.builder()
        .userId(userId)
        .message(updated.isMessageAlert())
        .like(updated.isLikeAlert())
        .comment(updated.isCommentAlert())
        .follow(updated.isFollowAlert())
        .mention(updated.isMentionAlert())
        .build();
  }

  /**
   * 입력 받은 사용자의 알림 설정을 조회
   *
   * @implNote 현재는 임시로 userId를 하드코딩하고 있으며, 실제 서비스 적용 시 JWT 인증을 통해 사용자 ID를 추출하도록 변경 필요. 해당 사용자가 최초로
   *     알림 설정 시, 모든 알림 설정은 false로 반환
   * @param userId 알림 설정을 확인할 사용자 ID
   * @return NotificationInfo 변경된 알림 설정 정보를 담은 응답 DTO
   */
  @Transactional(readOnly = true)
  public NotificationInfo getNotifications(String userId) {

    if (userId == null) {
      log.error("[userId] is null. Can't get notifications.");
      throw new NotificationBadRequestException("[userId] 정보가 존재하지 않습니다.");
    }

    Notification notification =
        notificationRepository
            .findByUserId(userId)
            .orElse(
                Notification.builder()
                    .userId(userId)
                    .messageAlert(false)
                    .likeAlert(false)
                    .commentAlert(false)
                    .followAlert(false)
                    .mentionAlert(false)
                    .build());

    log.info("Notification settings: {}", notification);

    return NotificationInfo.builder()
        .userId(userId)
        .message(notification.isMessageAlert())
        .like(notification.isLikeAlert())
        .comment(notification.isCommentAlert())
        .follow(notification.isFollowAlert())
        .mention(notification.isMentionAlert())
        .build();
  }
}
