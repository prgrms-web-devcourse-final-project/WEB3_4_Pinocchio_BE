package sns.pinocchio.application.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.application.notification.dto.NotificationResponseDto.NotificationInfo;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.notification.Notification;
import sns.pinocchio.infrastructure.persistence.mysql.NotificationRepository;
import sns.pinocchio.presentation.notification.exception.NotificationException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @InjectMocks private NotificationService notificationService;

  @Mock private NotificationRepository notificationRepository;

  @Mock private CustomUserDetails userDetails;

  private final String userId = "test_user_123";

  private Member mockMember;

  @BeforeEach
  void setUp() {

    mockMember =
        Member.builder()
            .email("example@naver.com")
            .password("testPassword!")
            .name("testName")
            .nickname("testNickname")
            .build();

    // tsid 수동 설정
    ReflectionTestUtils.setField(mockMember, "id", 1L);
  }

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

    when(userDetails.getMember()).thenReturn(mockMember);
    when(notificationRepository.findByUsersId(1L)).thenReturn(Optional.empty());
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    NotificationInfo updated = notificationService.updateNotifications(userDetails, update);

    // then
    assertThat(updated).isNotNull();
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
            .users(mockMember)
            .messageAlert(false)
            .likeAlert(false)
            .commentAlert(false)
            .followAlert(false)
            .mentionAlert(false)
            .build();

    UpdateNotifications update =
        UpdateNotifications.builder()
            .message(true)
            .like(true)
            .comment(true)
            .follow(true)
            .mention(true)
            .build();

    when(userDetails.getMember()).thenReturn(mockMember);
    when(notificationRepository.findByUsersId(1L)).thenReturn(Optional.of(existed));
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    NotificationInfo updated = notificationService.updateNotifications(userDetails, update);

    // then
    assertThat(updated).isNotNull();
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
    when(notificationRepository.findByUsersId(1L)).thenReturn(Optional.empty());
    when(userDetails.getUserId()).thenReturn(1L);

    // when
    NotificationInfo info = notificationService.getNotifications(userDetails);

    // then
    assertThat(info).isNotNull();
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
            .messageAlert(false)
            .likeAlert(true)
            .commentAlert(false)
            .followAlert(true)
            .mentionAlert(false)
            .build();

    when(notificationRepository.findByUsersId(1L)).thenReturn(Optional.of(existed));
    when(userDetails.getUserId()).thenReturn(1L);

    // when
    NotificationInfo info = notificationService.getNotifications(userDetails);

    // then
    assertThat(info).isNotNull();
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
    String errorMsg = "사용자가 인증되지 않았습니다.";

    // when
    NotificationException exception =
        assertThrows(NotificationException.class, () -> notificationService.getNotifications(null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }
}
