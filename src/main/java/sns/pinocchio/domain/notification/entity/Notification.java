package sns.pinocchio.domain.notification.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;

@Document(collection = "notifications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

  @Id private String id; // 알림 ID

  @Indexed private String userId; // 알림 설정 사용자

  private boolean message; // DM 메시지 알림

  private boolean like; // 좋아요 알림

  private boolean comment; // 댓글 알림

  private boolean follow; // 팔로우 알림

  private boolean mention; // 멘션 알림

  private LocalDateTime updatedAt; // 알림 설정 변경 날짜

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
