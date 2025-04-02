package sns.pinocchio.application.search.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchInfo;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchPosts;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchPostsDetail;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.search.SearchSortType;
import sns.pinocchio.domain.search.SearchType;
import sns.pinocchio.infrastructure.persistence.mongodb.SearchRepositoryCustom;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

  private final SearchRepositoryCustom searchRepositoryCustom;

  /**
   * 검색 타입에 맞게 게시물, 해시태그, 유저 검색
   *
   * @param query 검색 키워드
   * @param type 검색 대상 (all / posts / hashtags / users)
   * @param limit 최대 결과 개수
   * @param sortBy 정렬 기준 (latest / popular / random)
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return SearchInfo 검색 정보
   * @implNote 현재 해시태그 및 유저 조회 로직은 없음 (기능 확정 시 추가 필요)
   */
  @Transactional
  public SearchInfo searchUsersOrPosts(
      String query, String type, int limit, String sortBy, String cursor) {

    // 검색 타입 체크: 유효하지 않으면, 400에러 반환
    SearchType searchType = SearchType.from(type);

    // 정렬 기준 체크
    SearchSortType sortType = SearchSortType.from(sortBy);

    // 게시물 조회
    SearchPosts posts = searchPosts(query, searchType, sortType, limit, cursor);

    // TODO: 해시태그 조회 로직 필요 (기능 확정 시 추가)

    // TODO: 유저 조회 로직 필요 (기능 확정 시 추가)

    return new SearchInfo(query, posts, null, null);
  }

  /**
   * 게시물 검색
   *
   * @param query 검색 키워드
   * @param searchType 검색 타입
   * @param sortType 정렬 기준
   * @param limit 최대 결과 개수
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return SearchPosts 게시물 검색 결과
   */
  @Transactional
  public SearchPosts searchPosts(
      String query, SearchType searchType, SearchSortType sortType, int limit, String cursor) {

    // 게시물 조회 (다음 데이터 판단을 위해 limit + 1)
    List<Post> posts =
        searchRepositoryCustom.searchPostByQueryWithCursor(
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

    return new SearchPosts(nextCursor, hasNext, searchPostsDetails);
  }
}
