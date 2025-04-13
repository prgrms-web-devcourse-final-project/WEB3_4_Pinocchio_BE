package sns.pinocchio.domain.post;

public enum SearchSortType {
  LATEST,
  RANDOM;

  /** String으로 받은 정렬 방식을 Enum으로 변경 */
  public static SearchSortType from(String sortBy) {

    // 정렬 정보가 없을 경우, 기본값 반환
    if (sortBy == null) {
      return LATEST;
    }

    try {
      return SearchSortType.valueOf(sortBy.toUpperCase());

    } catch (IllegalArgumentException e) {
      // 잘못된 값이 들어왔을 경우, 기본값 반환
      return LATEST;
    }
  }
}
