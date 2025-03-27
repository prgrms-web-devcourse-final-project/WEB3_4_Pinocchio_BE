package sns.pinocchio.domain.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 알림 ID

  private boolean message; // DM 메시지 알림

  private boolean like; // 좋아요 알림

  private boolean comment; // 댓글 알림

  private boolean follow; // 팔로우 알림

  private boolean mention; // 멘션 알림

  private LocalDateTime updatedAt; // 알림 설정 변경 날짜

  // todo: user 테이블 생성 시 아래 주석과 교체 필요
  private String userId;  // 알림 설정 사용자

  /*
   * @OneToOne(mappedBy = "user")
   * private Users users;  // 알림 설정 사용자
   * */

  /**
   * @implNote 기존 알림 설정을 모두 덮어쓰는 방식으로 동작하기 때문에, 모든 항목을 포함한 updateNotifications 요청값이 필요.
   * @param updateNotifications 클라이언트로부터 전달받은 알림 설정 값
   */
  public void update(UpdateNotifications updateNotifications) {

    this.message = updateNotifications.message();
    this.like = updateNotifications.like();
    this.comment = updateNotifications.comment();
    this.follow = updateNotifications.follow();
    this.mention = updateNotifications.mention();
    this.updatedAt = LocalDateTime.now();
  }
}
