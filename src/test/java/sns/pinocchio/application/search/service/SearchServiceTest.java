package sns.pinocchio.application.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchInfo;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchUsers;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.infrastructure.member.MemberRepository;
import sns.pinocchio.presentation.search.exception.SearchException;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

  @InjectMocks private SearchService searchService;

  @Mock private MemberRepository searchMemberRepository;

  private CustomUserDetails mockUserDetails;

  private Member mockMember1;

  private Member mockMember2;

  private Member mockMember3;

  @BeforeEach
  void setUp() {

    mockUserDetails = mock(CustomUserDetails.class);

    mockMember1 = Member.builder().name("테스트유저1").nickname("테스트유저닉네임1").build();
    mockMember2 = Member.builder().name("테스트유저2").nickname("테스트유저닉네임2").build();
    mockMember3 = Member.builder().name("테스트유저3").nickname("테스트유저닉네임3").build();

    //    https://image.com/post_1.jpg
  }

  @Test
  @DisplayName("검색 Success: 결과가 limit 이하인 경우")
  void searchUsersNoNextPageSuccessTest() {

    // given
    String query = "테스트";
    int limit = 2;
    String cursor = null;

    List<Member> mockMembers = List.of(mockMember1, mockMember2);

    when(searchMemberRepository.searchUsers(query, limit + 1, cursor)).thenReturn(mockMembers);

    // when
    SearchInfo result = searchService.searchUsers(mockUserDetails, query, limit, cursor);

    // then
    assertThat(result.getUsers()).hasSize(2);
    assertThat(result.isHasNext()).isFalse();
    assertThat(result.getNextCursor()).isNull();

    SearchUsers detail1 = result.getUsers().get(0);
    assertThat(detail1.getName()).isEqualTo(mockMember1.getName());
    assertThat(detail1.getNickname()).isEqualTo(mockMember1.getNickname());

    SearchUsers detail2 = result.getUsers().get(1);
    assertThat(detail2.getName()).isEqualTo(mockMember2.getName());
    assertThat(detail2.getNickname()).isEqualTo(mockMember2.getNickname());
  }

  @Test
  @DisplayName("검색 Success: hasNext가 true인 경우")
  void searchPostsWithNextPageSuccessTest() {
    // given
    String query = "제주도";
    int limit = 2;
    String cursor = "mockCursor";

    List<Member> mockMembers = List.of(mockMember1, mockMember2, mockMember3);

    when(searchMemberRepository.searchUsers(query, limit + 1, cursor)).thenReturn(mockMembers);

    // when
    SearchInfo result = searchService.searchUsers(mockUserDetails, query, limit, cursor);

    // then
    assertThat(result.getUsers()).hasSize(2);
    assertThat(result.isHasNext()).isTrue();
    assertThat(result.getNextCursor()).isNotNull();

    SearchUsers detail1 = result.getUsers().get(0);
    assertThat(detail1.getName()).isEqualTo(mockMember1.getName());
    assertThat(detail1.getNickname()).isEqualTo(mockMember1.getNickname());

    SearchUsers detail2 = result.getUsers().get(1);
    assertThat(detail2.getName()).isEqualTo(mockMember2.getName());
    assertThat(detail2.getNickname()).isEqualTo(mockMember2.getNickname());
  }

  @Test
  @DisplayName("검색 Fail: 인증 정보를 찾을 수 없을 경우")
  void searchUsersUnauthorizedFailTest() {

    // given
    String errorMsg = "유효하지 않는 인증 정보입니다.";

    // when
    SearchException exception =
        assertThrows(SearchException.class, () -> searchService.searchUsers(null, null, 0, null));

    // then
    assertThat(exception.getMessage()).isEqualTo(errorMsg);
  }
}
