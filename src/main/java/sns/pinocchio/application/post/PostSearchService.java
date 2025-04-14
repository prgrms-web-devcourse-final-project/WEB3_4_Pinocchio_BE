package sns.pinocchio.application.post;

import static sns.pinocchio.presentation.post.exception.PostErrorCode.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.post.PostSearchResponse.SearchPosts;
import sns.pinocchio.application.post.PostSearchResponse.SearchPostsDetail;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.SearchSortType;
import sns.pinocchio.domain.post.SearchType;
import sns.pinocchio.infrastructure.persistence.mongodb.PostSearchRepositoryCustom;
import sns.pinocchio.presentation.member.exception.MemberException;
import sns.pinocchio.presentation.post.exception.PostErrorCode;
import sns.pinocchio.presentation.post.exception.PostException;
import sns.pinocchio.presentation.search.exception.SearchErrorCode;
import sns.pinocchio.presentation.search.exception.SearchException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostSearchService {

  private final PostSearchRepositoryCustom postSearchRepository;
  private final MemberService memberService;

  /**
   * 게시물 검색
   *
   * @param userDetails 로그인된 유저
   * @param query 검색 키워드
   * @param type 검색 타입
   * @param sortBy 정렬 기준
   * @param limit 최대 결과 개수
   * @param cursor 페이징 커서 (생성 날짜 기준)
   * @return SearchPosts 게시물 검색 결과
   * @throws PostException 게시글 검색에 대한 권한이 없을 경우 {@link PostErrorCode#UNAUTHORIZED_POST_SEARCH_ACCESS} 예외 발생
   * @throws PostException 커서의 날짜 형식이 올바르지 않을 경우 {@link PostErrorCode#INVALID_POST_SEARCH_CURSOR_TYPE} 예외 발생
   * @throws PostException 검색할 유저 정보를 찾을 수 없을 경우 {@link PostErrorCode#POST_SEARCH_USER_NOT_FOUND} 예외 발생
   */
  @Transactional
  public SearchPosts searchPosts(
      CustomUserDetails userDetails,
      String query,
      String type,
      String sortBy,
      int limit,
      String cursor) {

    // 로그인한 유저가 존재하지 않을 경우
    if (userDetails == null || userDetails.getMember() == null) {
      log.error("No authenticated user found.");
      throw new PostException(UNAUTHORIZED_POST_SEARCH_ACCESS);
    }

    Member user = userDetails.getMember();
    SearchType searchType = SearchType.from(type);
    SearchSortType searchSortType = SearchSortType.from(sortBy);

    // 커서 포맷 변경 (string -> LocalDateTime)
    LocalDateTime cursorDateTime = null;

    if (cursor != null) {
      try {
        cursorDateTime = LocalDateTime.parse(cursor);

      } catch (DateTimeParseException e) {
        log.error("Invalid cursor format: {}", cursor);
        throw new PostException(INVALID_POST_SEARCH_CURSOR_TYPE);
      }
    }

    // 검색 타입에 따라 게시글 조회 (다음 데이터 판단을 위해 limit + 1)
    List<Post> posts = new ArrayList<>();

    switch (searchType) {
      case USERS -> {
        String queryUser = query;

        if (queryUser == null) {
          // 검색 키워드가 존재하지 않으면, 현재 로그인 된 유저의 게시글 조회
          queryUser = user.getTsid();

        } else {
          // 검색 키워드가 존재할 경우, 검색할 유저의 정보가 존재하지 않으면 404에러 반환
          try {
            Member targetUser = memberService.findByNickname(queryUser);
            queryUser = targetUser.getTsid();

          } catch (MemberException e) {
            log.error("Failed to search posts: 'USERS' type requires a query.");
            throw new PostException(POST_SEARCH_USER_NOT_FOUND);
          }
        }

        posts =
            postSearchRepository.searchPostsByUserTsidWithCursor(
                queryUser, searchType, searchSortType, limit + 1, cursorDateTime);
      }

      case POSTS ->
          posts =
              postSearchRepository.searchPostByQueryWithCursor(
                  query, searchType, searchSortType, limit + 1, cursorDateTime);
    }

    log.info("Found Posts: count {}, data {}", posts.size(), posts);

    // hasNext 판단: 이후 데이터가 존재하지 않으면 false
    boolean hasNext = posts.size() > limit;

    // 실제 응답에 보낼 데이터는 limit까지만 저장
    List<Post> sliced = hasNext ? posts.subList(0, limit) : posts;

    List<SearchPostsDetail> searchPostsDetails =
        sliced.stream().map(SearchPostsDetail::toDetail).toList();

    // nextCursor 판단: 이후 데이터가 존재하지 않으면 null
    String nextCursor = hasNext ? sliced.getLast().getCreatedAt().toString() : null;

    return new SearchPosts(query, nextCursor, hasNext, searchPostsDetails);
  }
}
