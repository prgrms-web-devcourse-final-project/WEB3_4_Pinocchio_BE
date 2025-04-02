package sns.pinocchio.infrastructure.blockedUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.blockedUser.BlockedUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Integer> {
    Optional<BlockedUser> findByBlockerUserIdAndBlockedUserId(Long blockerId, Long blockedId);

    boolean existsByBlockerUserIdAndBlockedUserId(Long blockerId, Long blockedId);

    List<BlockedUser> findByBlockerUserId(Long blockerUserId);
}
