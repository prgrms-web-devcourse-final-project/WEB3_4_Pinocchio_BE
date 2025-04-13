package sns.pinocchio.infrastructure.persistence.mongodb;

import java.time.LocalDateTime;
import java.util.List;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.SearchSortType;
import sns.pinocchio.domain.post.SearchType;

public interface PostSearchRepositoryCustom {

  List<Post> searchPostByQueryWithCursor(
      String query,
      SearchType searchType,
      SearchSortType searchSortType,
      int limit,
      LocalDateTime cursor);

  List<Post> searchPostsByUserTsidWithCursor(
      String userTsid,
      SearchType searchType,
      SearchSortType searchSortType,
      int limit,
      LocalDateTime cursor);
}
