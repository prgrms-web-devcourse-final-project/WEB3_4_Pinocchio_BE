package sns.pinocchio.presentation.search;

import static sns.pinocchio.infrastructure.shared.response.GlobalApiResponse.success;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchInfo;
import sns.pinocchio.application.search.service.SearchService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;
import sns.pinocchio.infrastructure.shared.response.GlobalApiResponse;
import sns.pinocchio.infrastructure.shared.swagger.ErrorResponseSchema;

@Tag(name = "검색", description = "검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

  private final SearchService searchService;

  @Operation(summary = "사용자 검색", description = "사용자에 대해서 검색합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "검색 성공",
        content =
            @Content(
                schema = @Schema(implementation = SearchInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
  })
  @GetMapping
  public ResponseEntity<GlobalApiResponse<SearchInfo>> searchUsersOrPosts(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "limit", required = false, defaultValue = "9") int limit,
      @RequestParam(name = "cursor", required = false) String cursor) {

    SearchInfo searchInfo = searchService.searchUsers(userDetails, query, limit, cursor);

    return ResponseEntity.ok(
        success("%s 검색 결과입니다.".formatted(query == null ? "전체" : query), searchInfo));
  }
}
