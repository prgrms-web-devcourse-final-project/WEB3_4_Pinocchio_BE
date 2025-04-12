package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.mongodb.repository.MongoRepository;
import sns.pinocchio.domain.post.Post;

import java.util.List;
import java.util.Optional;
//몽고db post

public interface PostRepository extends MongoRepository<Post, String>, PostRepositoryCustom{
    // post 본문 수정
    Optional<Post> findByIdAndTsidAndStatus(String id, String tsid, String status);

    // 테스트 코드를 위해서 추가
    List<Post> findAllByTsid(String tsid);

    // 게시물 조회 (status가 active인 것만)
    Optional<Post> findByIdAndStatus(String id, String status);

}