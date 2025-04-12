package sns.pinocchio.infrastructure.persistence.mongodb;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.post.Post;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public void incrementLikesCount(String postId, int delta) {
        Query query = new Query(Criteria.where("_id").is(postId));
        Update update = new Update().inc("likes", delta);

        UpdateResult result = mongoTemplate.updateFirst(query, update, Post.class);
        // 필요하다면 result.getModifiedCount() 등으로 처리 결과 확인 가능
    }
}