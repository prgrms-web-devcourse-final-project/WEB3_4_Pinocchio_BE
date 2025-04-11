package sns.pinocchio.application.search.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.shared.response.GlobalCursorPageResponse;

public class SearchResponseDto {

  @Getter
  public static class SearchInfo extends GlobalCursorPageResponse {

    private final String query;

    private final List<SearchUsers> users;

    public SearchInfo(String query, String nextCursor, boolean hasNext, List<SearchUsers> users) {
      super(nextCursor, hasNext);
      this.query = query;
      this.users = users;
    }
  }

  @Getter
  @Builder
  public static class SearchUsers {

    private Long userId;

    private String userIdForSearch;

    private String name;

    private String nickname;

    private String profileImage;

    /**
     * Member entity -> SearchUsers Dto
     *
     * @param member 회원 정보
     * @return SearchUsers 회원 검색 정보
     */
    public static SearchUsers toDetail(Member member) {
      return SearchUsers.builder()
          .userId(member.getId())
          .userIdForSearch(member.getTsid())
          .name(member.getName())
          .nickname(member.getNickname())
          .profileImage(member.getProfileImageUrl())
          .build();
    }
  }
}
