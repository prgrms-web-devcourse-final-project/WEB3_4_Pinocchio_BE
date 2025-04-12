package sns.pinocchio.application.post;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.post.PostSearchResponse.SearchPosts;
import sns.pinocchio.application.post.PostSearchResponse.SearchPostsDetail;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.SearchSortType;
import sns.pinocchio.domain.post.SearchType;
import sns.pinocchio.infrastructure.persistence.mongodb.PostSearchRepositoryCustom;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostSearchService {

  private final PostSearchRepositoryCustom postSearchRepository;

  /**
   * 게시물 검색
   *
   * @param userDetails 로그인된 유저
   * @param query 검색 키워드
   * @param searchType 검색 타입
   * @param sortType 정렬 기준
   * @param limit 최대 결과 개수
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return SearchPosts 게시물 검색 결과
   */
  @Transactional
  public SearchPosts searchPosts(
      CustomUserDetails userDetails,
      String query,
      SearchType searchType,
      SearchSortType sortType,
      int limit,
      String cursor) {

    //    // 로그인한 유저가 존재하지 않을 경우
    //    if (userDetails == null) {
    //      log.error("No authenticated user found.");
    //      throw new IllegalArgumentException("유효하지 않은 인증입니다.");
    //    }

    // 게시물 조회 (다음 데이터 판단을 위해 limit + 1)
    List<Post> posts =
        postSearchRepository.searchPostByQueryWithCursor(
            query, searchType, sortType, limit + 1, cursor);

    log.info("Found Posts: count {}, data {}", posts.size(), posts);

    // hasNext 판단: 이후 데이터가 존재하지 않으면 false
    boolean hasNext = posts.size() > limit;

    // 실제 응답에 보낼 데이터는 limit까지만 저장
    List<Post> sliced = hasNext ? posts.subList(0, limit) : posts;

    List<SearchPostsDetail> searchPostsDetails =
        sliced.stream().map(SearchPostsDetail::toDetail).toList();

    // nextCursor 판단: 이후 데이터가 존재하지 않으면 null
    String nextCursor = hasNext ? sliced.getLast().getCreatedAt().toString() : null;

    return new SearchPosts(nextCursor, nextCursor, hasNext, searchPostsDetails);
  }
}
