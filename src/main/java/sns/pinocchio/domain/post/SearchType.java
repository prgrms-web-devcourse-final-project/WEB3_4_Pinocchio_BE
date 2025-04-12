package sns.pinocchio.domain.post;

import java.util.Arrays;

public enum SearchType {
  ALL,
  POSTS,
  HASHTAGS,
  USERS;

  /** String으로 받은 검색 타입을 Enum으로 변경 */
  public static SearchType from(String type) {

    if (type == null) {
      throw new IllegalArgumentException("검색 타입이 존재하지 않습니다.");
    }

    return Arrays.stream(values())
        .filter(t -> t.name().equalsIgnoreCase(type))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("유효하지 않는 검색 타입입니다: " + type));
  }
}
