package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.SearchSortType;
import sns.pinocchio.domain.post.SearchType;
import sns.pinocchio.domain.post.Visibility;

@Repository
@RequiredArgsConstructor
public class PostSearchRepositoryCustomImpl implements PostSearchRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Post> searchPostByQueryWithCursor(
      String query,
      SearchType searchType,
      SearchSortType searchSortType,
      int limit,
      String cursor) {

    List<Criteria> conditions = new ArrayList<>();

    // 비공개/삭제 제외
    Criteria baseCriteria =
        new Criteria()
            .andOperator(
                Criteria.where("visibility").ne(Visibility.PRIVATE),
                Criteria.where("status").ne("deleted"));
    conditions.add(baseCriteria);

    // 검색 키워드 존재 시, 본문/해시태그 포함 조건 적용
    if (query != null) {
      conditions.add(
          new Criteria()
              .orOperator(
                  Criteria.where("content").regex(".*" + Pattern.quote(query) + ".*", "i"),
                  Criteria.where("hashtags").regex(".*" + Pattern.quote(query) + ".*", "i")));
    }

    // RANDOM이 아닌 경우 커서 조건 추가
    if (cursor != null && searchSortType != SearchSortType.RANDOM) {
      if (searchSortType == SearchSortType.LATEST) {
        conditions.add(Criteria.where("createdAt").lt(cursor));

      } else if (searchSortType == SearchSortType.POPULAR) {
        conditions.add(Criteria.where("likes").lt(Integer.parseInt(cursor)));
      }
    }

    Criteria combinedCriteria = new Criteria().andOperator(conditions.toArray(new Criteria[0]));

    // 정렬 방식이 랜덤일 경우, Aggregation 사용하여 랜덤 처리
    if (searchSortType == SearchSortType.RANDOM) {
      MatchOperation matchStage = Aggregation.match(combinedCriteria);
      SampleOperation sampleStage = Aggregation.sample(limit);
      Aggregation aggregation = Aggregation.newAggregation(matchStage, sampleStage);

      return mongoTemplate.aggregate(aggregation, "posts", Post.class).getMappedResults();
    }

    // Query 기반 정렬
    Query q = new Query(combinedCriteria);

    // 정렬 필드 지정
    switch (searchSortType) {
      case LATEST -> q.with(Sort.by(Sort.Direction.DESC, "createdAt"));
      case POPULAR -> q.with(Sort.by(Sort.Direction.DESC, "likes"));
      default -> q.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // 제한 개수 설정
    q.limit(limit);

    return mongoTemplate.find(q, Post.class, "posts");
  }
}
