package sns.pinocchio.application.search.service;

import static sns.pinocchio.presentation.search.exception.SearchErrorCode.UNAUTHORIZED_USER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchInfo;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchUsers;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.search.exception.SearchErrorCode;
import sns.pinocchio.presentation.search.exception.SearchException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchService {

  private final MemberRepository searchMemberRepository;

  /**
   * 유저 검색
   *
   * @param userDetails 로그인한 유저 정보
   * @param query 검색 키워드
   * @param limit 최대 결과 개수
   * @param cursor 페이징 커서 (userTsid)
   * @return SearchInfo 사용자 검색 정보
   * @throws SearchException 인증되지 않은 사용자일 경우 {@link SearchErrorCode#UNAUTHORIZED_USER} 예외 발생
   */
  @Transactional
  public SearchInfo searchUsers(
      CustomUserDetails userDetails, String query, int limit, String cursor) {

    // 유저 정보 체크: 유효하지 않으면, 401에러 반환
    if (userDetails == null) {
      log.error("No authenticated user found.");
      throw new SearchException(UNAUTHORIZED_USER);
    }

    // 유저 조회 (다음 데이터 판단을 위해 limit + 1)
    List<Member> members = searchMemberRepository.searchUsers(query, limit + 1, cursor);

    log.info("Found Members: count {}, data {}", members.size(), members);

    // hasNext 판단: 이후 데이터가 존재하지 않으면 false
    boolean hasNext = members.size() > limit;

    // 실제 응답에 보낼 데이터는 limit 까지만 저장
    List<Member> sliced = hasNext ? members.subList(0, limit) : members;

    // nextCursor 판단: 이후 데이터가 존재하지 않으면 null
    String nextCursor = hasNext ? sliced.getLast().getTsid() : null;

    // 응답 데이터 생성: Member Entity -> SearchUsers Dto
    List<SearchUsers> searchUsers = sliced.stream().map(SearchUsers::toDetail).toList();

    return new SearchInfo(query, nextCursor, hasNext, searchUsers);
  }
}
