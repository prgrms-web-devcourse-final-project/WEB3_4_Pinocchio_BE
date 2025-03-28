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

    // 테스트 코드를 위해서 추가
    List<Post> findAllByUserTsid(String userTsid);

    // 삭제되지 않은 단일 게시글 조회
    Optional<Post> findByIdAndStatus(String id, String status);

    // 특정 사용자의 게시글 중 삭제되지 않은 것만 조회
    List<Post> findAllByUserTsidAndStatus(String userTsid, String status);

}