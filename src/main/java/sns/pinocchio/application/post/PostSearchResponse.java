package sns.pinocchio.application.post;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.shared.response.GlobalCursorPageResponse;

public class PostSearchResponse {

  @Getter
  public static class SearchPosts extends GlobalCursorPageResponse {

    private final String query;

    private final List<SearchPostsDetail> posts;

    public SearchPosts(
        String query, String nextCursor, boolean hasNext, List<SearchPostsDetail> posts) {
      super(nextCursor, hasNext);
      this.query = query;
      this.posts = posts;
    }
  }

  @Getter
  @Builder
  public static class SearchPostsDetail {

    private String postId;

    private List<String> imageUrl;

    private String createdAt;

    private int likes;

    private int commentsCount;

    /**
     * Post entity -> SearchPostDetail Dto
     *
     * @param post 게시물 정보
     * @return SearchPostDetail 게시물 검색 정보
     */
    public static SearchPostsDetail toDetail(Post post) {
      return SearchPostsDetail.builder()
          .postId(post.getId())
          .imageUrl(post.getImageUrls())
          .createdAt(post.getCreatedAt().toString())
          .likes(post.getLikes())
          .commentsCount(post.getCommentsCount())
          .build();
    }
  }
}
