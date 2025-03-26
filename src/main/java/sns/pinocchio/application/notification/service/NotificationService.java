package sns.pinocchio.application.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.domain.notification.entity.Notification;
import sns.pinocchio.infrastructure.persistence.mongodb.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  /**
   * 사용자의 알림 설정을 업데이트
   *
   * @implNote 현재는 임시로 userId를 하드코딩하고 있으며, 실제 서비스 적용 시 JWT 인증을 통해 사용자 ID를 추출하도록 변경 필요
   * @param updateNotifications 변경할 알림 설정 값들을 담은 DTO
   * @return NotificationInfo 변경된 알림 설정 정보를 담은 응답 DTO
   */
  public NotificationInfo updateNotifications(UpdateNotifications updateNotifications) {

    // TODO : 회원 인증 필요 (임시 변수 삭제 필요)
    String userId = "user_123";

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

    return NotificationInfo.builder()
        .userId(userId)
        .message(updated.isMessage())
        .like(updated.isLike())
        .comment(updated.isComment())
        .follow(updated.isFollow())
        .mention(updated.isMention())
        .build();
  }
}