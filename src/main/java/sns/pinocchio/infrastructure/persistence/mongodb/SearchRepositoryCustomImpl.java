package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.Visibility;
import sns.pinocchio.domain.search.SearchSortType;
import sns.pinocchio.domain.search.SearchType;

@Repository
@RequiredArgsConstructor
public class SearchRepositoryCustomImpl implements SearchRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  @Override
  public List<Post> searchPostByQueryWithCursor(
      String query,
      SearchType searchType,
      SearchSortType searchSortType,
      int limit,
      String cursor) {

    Query q = new Query();

    // 검색 타입에 따라 검색 조건 추가
    if (query != null) {
      switch (searchType) {
        case POSTS ->
            q.addCriteria(Criteria.where("content").regex(".*" + Pattern.quote(query) + ".*", "i"));
        case USERS ->
            q.addCriteria(Criteria.where("tsid").regex(".*" + Pattern.quote(query) + ".*", "i"));
        case HASHTAGS ->
            q.addCriteria(
                Criteria.where("hashtags").regex(".*" + Pattern.quote(query) + ".*", "i"));
        default -> {
          Criteria orCriteria =
              new Criteria()
                  .orOperator(
                      Criteria.where("content").regex(".*" + Pattern.quote(query) + ".*", "i"),
                      Criteria.where("tsid").regex(".*" + Pattern.quote(query) + ".*", "i"),
                      Criteria.where("hashtags").regex(".*" + Pattern.quote(query) + ".*", "i"));
          q.addCriteria(orCriteria);
        }
      }
    }

    // 비공개 게시물은 제외
    q.addCriteria(Criteria.where("visibility").ne(Visibility.PRIVATE));

    // 삭제된 게시물은 제외
    q.addCriteria(Criteria.where("status").ne("deleted"));

    // 커서가 있을 경우, 생성 시간 기준으로 이전 것만 가져오기
    if (cursor != null) {
      q.addCriteria(Criteria.where("createdAt").lt(cursor));
    }

    // 정렬 타입에 맞게, 생성 시간 기준으로 정렬
    // TODO: POPULAR와 RANDOM에 대한 기준점 및 구현이 필요함 (현재 날짜 기준으로 정렬하도록 임시 처리)
    switch (searchSortType) {
      case LATEST -> q.with(Sort.by(Sort.Direction.DESC, "createdAt"));
      case POPULAR -> q.with(Sort.by(Sort.Direction.DESC, "createdAt"));
      case RANDOM -> q.with(Sort.by(Sort.Direction.DESC, "createdAt"));
      default -> q.with(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // 제한 개수 설정
    q.limit(limit);

    return mongoTemplate.find(q, Post.class, "posts");
  }
}
