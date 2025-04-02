package sns.pinocchio.application.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sns.pinocchio.application.search.dto.SearchResponseDto;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchInfo;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchPosts;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.search.SearchSortType;
import sns.pinocchio.domain.search.SearchType;
import sns.pinocchio.infrastructure.persistence.mongodb.SearchRepositoryCustom;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @InjectMocks private SearchService searchService;

  @Mock private SearchRepositoryCustom searchRepositoryCustom;

  private Post mockPost1;

  private Post mockPost2;

  private Post mockPost3;

  @BeforeEach
  void setUp() {

    mockPost1 =
        Post.builder()
            .id("post_1")
            .tsid("user_1")
            .imageUrls(List.of("https://image.com/post_1.jpg"))
            .createdAt(LocalDateTime.now())
            .content("테스트 문서입니다.")
            .hashtags(List.of("#테스트"))
            .build();

    mockPost2 =
        Post.builder()
            .id("post_2")
            .tsid("user_2")
            .imageUrls(List.of("https://image.com/post_2.jpg"))
            .createdAt(LocalDateTime.now())
            .content("테스트 문서입니다.")
            .hashtags(List.of("#테스트"))
            .build();

    mockPost3 =
        Post.builder()
            .id("post_3")
            .tsid("user_3")
            .imageUrls(List.of("https://image.com/post_3.jpg"))
            .createdAt(LocalDateTime.now())
            .content("테스트 문서입니다.")
            .hashtags(List.of("#테스트"))
            .build();
  }

  @Test
  @DisplayName("검색 Success: 결과가 limit 이하인 경우")
  void searchPostsNoNextPageSuccessTest() {

    // given
    String query = "제주도";
    SearchType searchType = SearchType.HASHTAGS;
    SearchSortType sortType = SearchSortType.LATEST;
    int limit = 2;
    String cursor = null;

    List<Post> mockPosts = List.of(mockPost1, mockPost2);

    when(searchRepositoryCustom.searchPostByQueryWithCursor(
            query, searchType, sortType, limit + 1, cursor))
        .thenReturn(mockPosts);

    // when
    SearchPosts result = searchService.searchPosts(query, searchType, sortType, limit, cursor);

    // then
    assertThat(result.getPosts()).hasSize(2);
    assertThat(result.isHasNext()).isFalse();
    assertThat(result.getNextCursor()).isNull();

    SearchResponseDto.SearchPostsDetail detail1 = result.getPosts().get(0);
    assertThat(detail1.getPostId()).isEqualTo(mockPost1.getId());
    assertThat(detail1.getUserId()).isEqualTo(mockPost1.getTsid());
    assertThat(detail1.getProfileImageUrl()).containsExactlyElementsOf(mockPost1.getImageUrls());

    SearchResponseDto.SearchPostsDetail detail2 = result.getPosts().get(1);
    assertThat(detail2.getPostId()).isEqualTo(mockPost2.getId());
    assertThat(detail2.getUserId()).isEqualTo(mockPost2.getTsid());
    assertThat(detail2.getProfileImageUrl()).containsExactlyElementsOf(mockPost2.getImageUrls());
  }

  @Test
  @DisplayName("검색 Success: hasNext가 true인 경우")
  void searchPostsWithNextPageSuccessTest() {
    // given
    String query = "제주도";
    SearchType searchType = SearchType.POSTS;
    SearchSortType sortType = SearchSortType.LATEST;
    int limit = 2;
    String cursor = null;

    List<Post> mockPosts = List.of(mockPost1, mockPost2, mockPost3);

    when(searchRepositoryCustom.searchPostByQueryWithCursor(
            query, searchType, sortType, limit + 1, cursor))
        .thenReturn(mockPosts);

    // when
    SearchPosts result = searchService.searchPosts(query, searchType, sortType, limit, cursor);

    // then
    assertThat(result.getPosts()).hasSize(2);
    assertThat(result.isHasNext()).isTrue();

    SearchResponseDto.SearchPostsDetail detail1 = result.getPosts().get(0);
    assertThat(detail1.getPostId()).isEqualTo(mockPost1.getId());
    assertThat(detail1.getUserId()).isEqualTo(mockPost1.getTsid());
    assertThat(detail1.getProfileImageUrl()).containsExactlyElementsOf(mockPost1.getImageUrls());

    SearchResponseDto.SearchPostsDetail detail2 = result.getPosts().get(1);
    assertThat(detail2.getPostId()).isEqualTo(mockPost2.getId());
    assertThat(detail2.getUserId()).isEqualTo(mockPost2.getTsid());
    assertThat(detail2.getProfileImageUrl()).containsExactlyElementsOf(mockPost2.getImageUrls());
  }

  @Test
  @DisplayName("통합 검색 Success")
  void searchUsersOrPostsSuccess() {

    // TODO: 해시태그 및 유저는 아직 미완성

    // given
    String query = "제주도";
    String type = "posts";
    String sortBy = "latest";
    int limit = 2;
    String cursor = null;

    when(searchRepositoryCustom.searchPostByQueryWithCursor(
            query, SearchType.POSTS, SearchSortType.LATEST, limit + 1, cursor))
        .thenReturn(List.of());

    // when
    SearchInfo result = searchService.searchUsersOrPosts(query, type, limit, sortBy, cursor);

    // then
    assertThat(result.getQuery()).isEqualTo("제주도");
    assertThat(result.getPosts()).isNotNull();
    assertThat(result.getHashtags()).isNull();
    assertThat(result.getUsers()).isNull();
  }
}
