package sns.pinocchio.infrastructure.blockedUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.blockedUser.BlockedUser;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Integer> {
}
