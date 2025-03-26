package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import sns.pinocchio.domain.post.Post;
//몽고db post

public interface PostRepository extends MongoRepository<Post, String> {
    // 필요 시 커스텀 쿼리 작성
}