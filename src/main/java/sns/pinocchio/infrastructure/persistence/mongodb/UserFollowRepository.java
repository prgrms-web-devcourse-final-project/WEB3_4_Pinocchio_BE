package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import sns.pinocchio.domain.user.UserFollow;

public interface UserFollowRepository extends MongoRepository<UserFollow, String> {
	Optional<UserFollow> findByFollowerIdAndFollowingId(String followerId, String followingId);
}
