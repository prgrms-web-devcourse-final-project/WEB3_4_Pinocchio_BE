package sns.pinocchio.application.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.domain.notification.Notification;
import sns.pinocchio.infrastructure.persistence.mysql.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks private NotificationService notificationService;

  @Mock private NotificationRepository notificationRepository;

  private final String userId = "test_user_123";

  @Test
  @DisplayName("알림 설정 Success: 기존 설정이 없는 경우")
  void updateNotificationsNotExistTest() {

    // given
    UpdateNotifications update =
        UpdateNotifications.builder()
            .message(true)
            .like(true)
            .comment(true)
            .follow(true)
            .mention(true)
            .build();

    when(notificationRepository.findByUserId(userId)).thenReturn(Optional.empty());
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    NotificationInfo updated = notificationService.updateNotifications(userId, update);

    // then
    assertThat(updated).isNotNull();
    assertThat(updated.userId()).isEqualTo(userId);
    assertThat(updated.message()).isTrue();
    assertThat(updated.like()).isTrue();
    assertThat(updated.comment()).isTrue();
    assertThat(updated.follow()).isTrue();
    assertThat(updated.mention()).isTrue();
  }

  @Test
  @DisplayName("알림 설정 Success: 기존 설정이 있는 경우")
  void updateNotificationsExistTest() {

    // given
    Notification existed =
        Notification.builder()
            .id(1L)
            .userId(userId)
            .message(false)
            .like(false)
            .comment(false)
            .follow(false)
            .mention(false)
            .build();

    UpdateNotifications update =
        UpdateNotifications.builder()
            .message(true)
            .like(true)
            .comment(true)
            .follow(true)
            .mention(true)
            .build();

    when(notificationRepository.findByUserId(userId)).thenReturn(Optional.of(existed));
    when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    NotificationInfo updated = notificationService.updateNotifications(userId, update);

    // then
    assertThat(updated).isNotNull();
    assertThat(updated.userId()).isEqualTo(userId);
    assertThat(updated.message()).isTrue();
    assertThat(updated.like()).isTrue();
    assertThat(updated.comment()).isTrue();
    assertThat(updated.follow()).isTrue();
    assertThat(updated.mention()).isTrue();
  }

  @Test
  @DisplayName("알림 설정 조회 Success: 기존 알람 설정이 존재하지 않을 경우")
  void getNotificationsNotExistTest() {

    // given
    when(notificationRepository.findByUserId(userId)).thenReturn(Optional.empty());

    // when
    NotificationInfo info = notificationService.getNotifications(userId);

    // then
    assertThat(info).isNotNull();
    assertThat(info.userId()).isEqualTo(userId);
    assertThat(info.message()).isFalse();
    assertThat(info.like()).isFalse();
    assertThat(info.comment()).isFalse();
    assertThat(info.follow()).isFalse();
    assertThat(info.mention()).isFalse();
  }

  @Test
  @DisplayName("알림 설정 조회 Success: 기존 알람 설정이 존재할 경우")
  void getNotificationsExistTest() {

    // given
    Notification existed =
        Notification.builder()
            .id(1L)
            .userId(userId)
            .message(false)
            .like(true)
            .comment(false)
            .follow(true)
            .mention(false)
            .build();

    when(notificationRepository.findByUserId(userId)).thenReturn(Optional.of(existed));

    // when
    NotificationInfo info = notificationService.getNotifications(userId);

    // then
    assertThat(info).isNotNull();
    assertThat(info.userId()).isEqualTo(userId);
    assertThat(info.message()).isFalse();
    assertThat(info.like()).isTrue();
    assertThat(info.comment()).isFalse();
    assertThat(info.follow()).isTrue();
    assertThat(info.mention()).isFalse();
  }

  @Test
  @DisplayName("알림 설정 조회 Fail: 유저 ID가 존재하지 않을 경우")
  void getNotificationsNoUserIdTest() {

    // given
    String errorMsg = "[userId] 정보가 존재하지 않습니다.";

    // when
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> notificationService.getNotifications(null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }
}
