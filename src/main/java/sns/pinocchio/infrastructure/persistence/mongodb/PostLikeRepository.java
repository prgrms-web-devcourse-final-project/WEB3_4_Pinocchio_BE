package sns.pinocchio.infrastructure.persistence.mongodb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import sns.pinocchio.config.global.enums.CancellState;
import sns.pinocchio.domain.post.PostLike;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends MongoRepository<PostLike, String> {

    //  좋아요 여부 확인용 (상세 조회에 사용)
    boolean existsByPostIdAndTsid(String postId, String tsid);

    // 특정 사용자가 특정 게시글에 좋아요 했는지 조회 (토글 여부 판단용)
	Optional<PostLike> findByPostIdAndTsid(String postId, String tsid);

	// 특정 게시글의 좋아요 수 (소프트 삭제 제외)
	long countByPostIdAndStatus(String postId, CancellState status);

	// 내가 좋아요 누른 게시글 리스트 조회
	Page<PostLike> findAllByTsidAndStatus(String tsid, CancellState status, Pageable pageable);

	// 특정 게시글에 좋아요 누른 전체 사용자 조회
	List<PostLike> findAllByPostIdAndStatus(String postId, CancellState status);

}
