package sns.pinocchio.application.search.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.infrastructure.shared.response.GlobalCursorPageResponse;

public class SearchResponseDto {

  @Getter
  @RequiredArgsConstructor
  public static class SearchInfo {

    private final String query;

    private final SearchPosts posts;

    private final SearchHashtags hashtags;

    private final SearchUsers users;
  }

  @Getter
  public static class SearchPosts extends GlobalCursorPageResponse {

    private final List<SearchPostsDetail> posts;

    public SearchPosts(String nextCursor, boolean hasNext, List<SearchPostsDetail> posts) {
      super(nextCursor, hasNext);
      this.posts = posts;
    }
  }

  @Getter
  public static class SearchHashtags extends GlobalCursorPageResponse {

    // TODO: 해시태그 로직 확정 시, 추가 필요

    public SearchHashtags(String nextCursor, boolean hasNext, List<SearchPostsDetail> posts) {
      super(nextCursor, hasNext);
    }
  }

  @Getter
  public static class SearchUsers extends GlobalCursorPageResponse {

    // TODO: 유저 로직 확정 시, 추가 필요

    public SearchUsers(String nextCursor, boolean hasNext) {
      super(nextCursor, hasNext);
    }
  }

  @Getter
  @Builder
  public static class SearchPostsDetail {

    private String postId;

    private String userId;

    private List<String> profileImageUrl;

    private String content;

    private List<String> hashtags;

    private int likes;

    private int commentsCount;

    private String createdAt;

    /**
     * Post entity -> SearchPostDetail Dto
     *
     * @param post 게시물 정보
     * @return SearchPostDetail 게시물 검색 정보
     */
    public static SearchPostsDetail toDetail(Post post) {
      return SearchPostsDetail.builder()
          .postId(post.getId())
          .userId(post.getTsid())
          .profileImageUrl(post.getImageUrls())
          .content(post.getContent())
          .hashtags(post.getHashtags())
          .likes(post.getLikes())
          .commentsCount(post.getCommentsCount())
          .createdAt(post.getCreatedAt().toString())
          .build();
    }
  }
}
