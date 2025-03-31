package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.domain.user.UserFollow;
import sns.pinocchio.domain.user.UserFollowStatus;

public interface UserFollowRepository extends MongoRepository<UserFollow, String> {
	Optional<UserFollow> findByFollowerIdAndFollowingId(String followerId, String followingId);

	Page<UserFollow> findAllByFollowerIdAndStatusOrderByUpdatedAtDesc(String followerId, Pageable pageable,
		UserFollowStatus status);

	Page<UserFollow> findAllByFollowingIdAndStatusOrderByUpdatedAtDesc(String followingId, Pageable pageable,
		UserFollowStatus status);
}
