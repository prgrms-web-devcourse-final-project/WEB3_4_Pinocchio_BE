package sns.pinocchio.domain.blockedUser;

import org.springframework.data.redis.core.RedisHash;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class BlockedUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false)
  private Long blockerUserId;

  @Column(nullable = false)
  private Long blockedUserId;

  @Builder
  public BlockedUser(Long blockerUserId, Long blockedUserId) {
    this.blockerUserId = blockerUserId;
    this.blockedUserId = blockedUserId;
  }
}
