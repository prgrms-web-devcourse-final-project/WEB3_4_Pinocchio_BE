package sns.pinocchio.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sns.pinocchio.application.post.PostSearchResponse.SearchPosts;
import sns.pinocchio.application.post.PostSearchResponse.SearchPostsDetail;
import sns.pinocchio.application.post.PostSearchService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.domain.post.Post;
import sns.pinocchio.domain.post.SearchSortType;
import sns.pinocchio.domain.post.SearchType;
import sns.pinocchio.infrastructure.persistence.mongodb.PostSearchRepositoryCustom;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class PostSearchServiceTest {

  @InjectMocks private PostSearchService postSearchService;

  @Mock private PostSearchRepositoryCustom postSearchRepository;

  private Post mockPost1;

  private Post mockPost2;

  private Post mockPost3;

  private Member mockMember1;

  private Member mockMember2;

  @BeforeEach
  void setUp() {

    mockPost1 =
        Post.builder()
            .id("post_1")
            .imageUrls(List.of("https://image.com/post_1.jpg"))
            .createdAt(LocalDateTime.now())
            .likes(1)
            .commentsCount(1)
            .build();

    mockPost2 =
        Post.builder()
            .id("post_2")
            .imageUrls(List.of("https://image.com/post_2.jpg"))
            .createdAt(LocalDateTime.now())
            .likes(2)
            .commentsCount(2)
            .build();

    mockPost3 =
        Post.builder()
            .id("post_3")
            .imageUrls(List.of("https://image.com/post_3.jpg"))
            .createdAt(LocalDateTime.now())
            .likes(3)
            .commentsCount(3)
            .build();

    mockMember1 = Member.builder().nickname("mockNickname1").build();

    mockMember2 = Member.builder().nickname("mockNickname2").build();
  }

  @Test
  @DisplayName("검색 Success: 결과가 limit 이하인 경우")
  void searchPostsNoNextPageSuccessTest() {

    // given
    String query = "제주도";
    SearchType searchType = SearchType.POSTS;
    SearchSortType sortType = SearchSortType.LATEST;
    int limit = 2;
    LocalDateTime cursor = null;
    CustomUserDetails mockUserDeatils = mock(CustomUserDetails.class);

    List<Post> mockPosts = List.of(mockPost1, mockPost2);

    when(postSearchRepository.searchPostByQueryWithCursor(
            query, searchType, sortType, limit + 1, cursor))
        .thenReturn(mockPosts);
    when(mockUserDeatils.getMember()).thenReturn(mockMember1);

    // when
    SearchPosts result =
        postSearchService.searchPosts(
            mockUserDeatils, query, searchType.toString(), sortType.toString(), limit, null);

    // then
    assertThat(result.getPosts()).hasSize(2);
    assertThat(result.isHasNext()).isFalse();
    assertThat(result.getNextCursor()).isNull();

    SearchPostsDetail detail1 = result.getPosts().get(0);
    assertThat(detail1.getPostId()).isEqualTo(mockPost1.getId());
    assertThat(detail1.getImageUrl()).containsExactlyElementsOf(mockPost1.getImageUrls());
    assertThat(detail1.getLikes()).isEqualTo(mockPost1.getLikes());
    assertThat(detail1.getCommentsCount()).isEqualTo(mockPost1.getCommentsCount());

    SearchPostsDetail detail2 = result.getPosts().get(1);
    assertThat(detail2.getPostId()).isEqualTo(mockPost2.getId());
    assertThat(detail2.getImageUrl()).containsExactlyElementsOf(mockPost2.getImageUrls());
    assertThat(detail2.getLikes()).isEqualTo(mockPost2.getLikes());
    assertThat(detail2.getCommentsCount()).isEqualTo(mockPost2.getCommentsCount());
  }

  @Test
  @DisplayName("검색 Success: hasNext가 true인 경우")
  void searchPostsWithNextPageSuccessTest() {
    // given
    String query = "제주도";
    SearchType searchType = SearchType.POSTS;
    SearchSortType sortType = SearchSortType.LATEST;
    int limit = 2;
    LocalDateTime cursor = null;
    CustomUserDetails mockUserDeatils = mock(CustomUserDetails.class);

    List<Post> mockPosts = List.of(mockPost1, mockPost2, mockPost3);

    when(postSearchRepository.searchPostByQueryWithCursor(
            query, searchType, sortType, limit + 1, cursor))
        .thenReturn(mockPosts);
    when(mockUserDeatils.getMember()).thenReturn(mockMember1);

    // when
    SearchPosts result =
        postSearchService.searchPosts(
            mockUserDeatils, query, searchType.toString(), sortType.toString(), limit, null);

    // then
    assertThat(result.getPosts()).hasSize(2);
    assertThat(result.isHasNext()).isTrue();
    assertThat(result.getNextCursor()).isNotNull();

    SearchPostsDetail detail1 = result.getPosts().get(0);
    assertThat(detail1.getPostId()).isEqualTo(mockPost1.getId());
    assertThat(detail1.getImageUrl()).containsExactlyElementsOf(mockPost1.getImageUrls());
    assertThat(detail1.getLikes()).isEqualTo(mockPost1.getLikes());
    assertThat(detail1.getCommentsCount()).isEqualTo(mockPost1.getCommentsCount());

    SearchPostsDetail detail2 = result.getPosts().get(1);
    assertThat(detail2.getPostId()).isEqualTo(mockPost2.getId());
    assertThat(detail2.getImageUrl()).containsExactlyElementsOf(mockPost2.getImageUrls());
    assertThat(detail2.getLikes()).isEqualTo(mockPost2.getLikes());
    assertThat(detail2.getCommentsCount()).isEqualTo(mockPost2.getCommentsCount());
  }
}
