package sns.pinocchio.application.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.domain.notification.Notification;
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

    // 회원의 알림 설정 조회
    Notification notification =
        notificationRepository
            .findByUserId(userId)
            .orElse(
                Notification.builder()
                    .userId(userId)
                    .message(updateNotifications.message())
                    .like(updateNotifications.like())
                    .comment(updateNotifications.comment())
                    .follow(updateNotifications.follow())
                    .mention(updateNotifications.mention())
                    .build());

    // 요청받은 알림 변경 사항들로 알림 수정
    notification.update(updateNotifications);

    // 수정된 알림 설정을 기반으로 DB 수정
    Notification updated = notificationRepository.save(notification);

    log.info("Notification settings updated: {}", updated);

    return NotificationInfo.builder()
        .userId(userId)
        .message(updated.isMessage())
        .like(updated.isLike())
        .comment(updated.isComment())
        .follow(updated.isFollow())
        .mention(updated.isMention())
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
      throw new IllegalArgumentException("[userId] 정보가 존재하지 않습니다.");
    }

    Notification notification =
        notificationRepository
            .findByUserId(userId)
            .orElse(
                Notification.builder()
                    .userId(userId)
                    .message(false)
                    .like(false)
                    .comment(false)
                    .follow(false)
                    .mention(false)
                    .build());

    return NotificationInfo.builder()
        .userId(userId)
        .message(notification.isMessage())
        .like(notification.isLike())
        .comment(notification.isComment())
        .follow(notification.isFollow())
        .mention(notification.isMention())
        .build();
  }
}
