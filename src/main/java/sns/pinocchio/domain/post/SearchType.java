package sns.pinocchio.domain.post;

public enum SearchType {
  POSTS,
  USERS;

  /** String으로 받은 검색 타입을 Enum으로 변경 */
  public static SearchType from(String type) {

    if (type == null) {
      return POSTS;
    }

    try {
      return SearchType.valueOf(type.toUpperCase());

    } catch (IllegalArgumentException e) {
      // 잘못된 값이 들어왔을 경우, 기본값 반환
      return POSTS;
    }
  }
}
