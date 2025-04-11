package sns.pinocchio.domain.notification;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import sns.pinocchio.application.notification.dto.NotificationRequestDto.UpdateNotifications;
import sns.pinocchio.domain.member.Member;

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

  private boolean messageAlert; // DM 메시지 알림

  private boolean likeAlert; // 좋아요 알림

  private boolean commentAlert; // 댓글 알림

  private boolean followAlert; // 팔로우 알림

  private boolean mentionAlert; // 멘션 알림

  private LocalDateTime updatedAt; // 알림 설정 변경 날짜

  @OneToOne
  @JoinColumn(name = "member_id")
  private Member users; // 알림 설정 사용자

  /**
   * @implNote 기존 알림 설정을 모두 덮어쓰는 방식으로 동작하기 때문에, 모든 항목을 포함한 updateNotifications 요청값이 필요.
   * @param updateNotifications 클라이언트로부터 전달받은 알림 설정 값
   */
  public void update(UpdateNotifications updateNotifications) {

    this.messageAlert = updateNotifications.message();
    this.likeAlert = updateNotifications.like();
    this.commentAlert = updateNotifications.comment();
    this.followAlert = updateNotifications.follow();
    this.mentionAlert = updateNotifications.mention();
    this.updatedAt = LocalDateTime.now();
  }
}
