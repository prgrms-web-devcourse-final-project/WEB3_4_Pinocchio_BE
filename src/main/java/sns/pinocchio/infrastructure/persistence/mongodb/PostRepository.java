package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import sns.pinocchio.domain.post.Post;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
//몽고db post

public interface PostRepository extends MongoRepository<Post, String> {
    // post 본문 수정
    Optional<Post> findByIdAndUserTsidAndStatus(String id, String userTsid, String status);

    List<Post> findAllByUserTsid(String userTsid);

}