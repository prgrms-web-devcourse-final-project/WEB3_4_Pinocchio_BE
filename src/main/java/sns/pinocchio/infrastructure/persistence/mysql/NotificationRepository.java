package sns.pinocchio.infrastructure.persistence.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.notification.Notification;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

  Optional<Notification> findByUserId(String userId);
}
