package sns.pinocchio.infrastructure.persistence.mysql;

import org.springframework.data.jpa.repository.JpaRepository;
import sns.pinocchio.domain.post.Hashtag;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Optional<Hashtag> findByTag(String tag);
}
