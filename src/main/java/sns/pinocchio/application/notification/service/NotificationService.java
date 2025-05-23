package sns.pinocchio.application.notification.service;

import static sns.pinocchio.presentation.notification.exception.NotificationErrorCode.INVALID_NOTIFICATION_TYPE;
import static sns.pinocchio.presentation.notification.exception.NotificationErrorCode.UNAUTHORIZED_NOTIFICATION_USER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.notification.Notification;
import sns.pinocchio.infrastructure.persistence.mysql.NotificationRepository;
import sns.pinocchio.presentation.notification.exception.NotificationErrorCode;
import sns.pinocchio.presentation.notification.exception.NotificationException;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

  /**
   * 입력받은 사용자의 알림 설정을 업데이트
   *
   * @param userDetails 로그인한 유저 정보
   * @param updateNotifications 변경할 알림 설정 값들을 담은 DTO
   * @return NotificationInfo 변경된 알림 설정 정보를 담은 응답 DTO
   * @throws NotificationException 로그인한 유저 정보가 존재하지 않을 경우 {@link
   *     NotificationErrorCode#UNAUTHORIZED_NOTIFICATION_USER} 예외 발생
   * @throws NotificationException 설정 정보가 존재하지 않을 경우 {@link
   *     NotificationErrorCode#INVALID_NOTIFICATION_TYPE} 예외 발생
   */
  @Transactional
  public NotificationInfo updateNotifications(
      CustomUserDetails userDetails, UpdateNotifications updateNotifications) {

    // 로그인한 유저 정보가 존재하지 않을 경우, 401에러 반환
    if (userDetails == null || userDetails.getMember() == null) {
      log.error("No authenticated user found.");
      throw new NotificationException(UNAUTHORIZED_NOTIFICATION_USER);
    }

    Member member = userDetails.getMember();

    // 설정 정보가 존재하지 않을 경우, 400에러 반환
    if (!updateNotifications.checkNotifications()) {
      log.error("The requested notification settings are invalid: {}", updateNotifications);
      throw new NotificationException(INVALID_NOTIFICATION_TYPE);
    }

    // 회원의 알림 설정 조회
    Notification notification =
        notificationRepository
            .findByUsersId(member.getId())
            .orElse(
                Notification.builder()
                    .messageAlert(updateNotifications.message())
                    .likeAlert(updateNotifications.like())
                    .commentAlert(updateNotifications.comment())
                    .followAlert(updateNotifications.follow())
                    .mentionAlert(updateNotifications.mention())
                    .users(member)
                    .build());

    // 요청받은 알림 변경 사항들로 알림 수정
    notification.update(updateNotifications);

    // 수정된 알림 설정을 기반으로 DB 수정
    Notification updated = notificationRepository.save(notification);

    log.info("Notification settings updated: {}", updated);

    return NotificationInfo.builder()
        .userId(member.getTsid())
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
   * @implNote 해당 사용자가 최초로 알림 설정 시, 모든 알림 설정은 false로 반환
   * @param userDetails 로그인한 사용자 정보
   * @return NotificationInfo 변경된 알림 설정 정보를 담은 응답 DTO
   * @throws NotificationException 로그인한 유저 정보가 존재하지 않을 경우 {@link
   *     NotificationErrorCode#UNAUTHORIZED_NOTIFICATION_USER} 예외 발생
   */
  @Transactional(readOnly = true)
  public NotificationInfo getNotifications(CustomUserDetails userDetails) {

    // 로그인한 유저 정보가 존재하지 않을 경우, 401에러 반환
    if (userDetails == null || userDetails.getUserId() == null) {
      log.error("[userId] is null. Can't get notifications.");
      throw new NotificationException(UNAUTHORIZED_NOTIFICATION_USER);
    }

    Notification notification =
        notificationRepository
            .findByUsersId(userDetails.getUserId())
            .orElse(
                Notification.builder()
                    .messageAlert(false)
                    .likeAlert(false)
                    .commentAlert(false)
                    .followAlert(false)
                    .mentionAlert(false)
                    .build());

    log.info("Notification settings: {}", notification);

    return NotificationInfo.builder()
        .userId(userDetails.getTsid())
        .message(notification.isMessageAlert())
        .like(notification.isLikeAlert())
        .comment(notification.isCommentAlert())
        .follow(notification.isFollowAlert())
        .mention(notification.isMentionAlert())
        .build();
  }
}
