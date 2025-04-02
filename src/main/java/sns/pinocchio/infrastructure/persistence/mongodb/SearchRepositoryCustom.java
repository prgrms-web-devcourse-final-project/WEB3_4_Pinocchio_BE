package sns.pinocchio.infrastructure.persistence.mongodb;

import java.util.List;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.search.SearchSortType;
import sns.pinocchio.domain.search.SearchType;

public interface SearchRepositoryCustom {

  List<Post> searchPostByQueryWithCursor(
      String query, SearchType searchType, SearchSortType searchSortType, int limit, String cursor);
}
