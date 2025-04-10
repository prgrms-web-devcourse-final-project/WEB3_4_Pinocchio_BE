package sns.pinocchio.infrastructure.persistence.mysql;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.notification.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Optional<Notification> findByUsersId(Long memberId);
}
