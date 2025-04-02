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
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.notification.dto.NotificationResponseDto;
import sns.pinocchio.application.search.dto.SearchResponseDto.SearchInfo;
import sns.pinocchio.application.search.service.SearchService;
import sns.pinocchio.infrastructure.shared.response.GlobalApiResponse;
import sns.pinocchio.infrastructure.shared.swagger.ErrorResponseSchema;

@Tag(name = "검색", description = "검색 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

  private final SearchService searchService;

  @Operation(summary = "검색", description = "사용자 또는 게시물에 대해서 검색합니다.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "검색 성공",
        content =
            @Content(
                schema = @Schema(implementation = NotificationResponseDto.NotificationInfo.class),
                mediaType = "application/json")),
    @ApiResponse(
        responseCode = "401",
        description = "인증 실패",
        content = @Content(schema = @Schema(implementation = ErrorResponseSchema.class)))
  })
  @GetMapping
  public ResponseEntity<GlobalApiResponse<Object>> searchUsersOrPosts(
      @RequestHeader(value = "Authorization") String accessToken,
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "type", required = false, defaultValue = "all") String type,
      @RequestParam(name = "limit", required = false, defaultValue = "9") int limit,
      @RequestParam(name = "sortBy", required = false, defaultValue = "latest") String sortBy,
      @RequestParam(name = "cursor", required = false) String cursor) {

    // TODO: JWT 토큰 인증 기능 완료 시, 회원 검증 절차 추가 필요

    // TODO: 현재 해시태그 검색만 가능 (추가 기능시 해제 필요)
    type = "HASHTAGS";

    SearchInfo searchInfo = searchService.searchUsersOrPosts(query, type, limit, sortBy, cursor);

    return ResponseEntity.ok(
        success("%s 검색 결과입니다.".formatted(query == null ? "전체" : "\"" + query + "\""), searchInfo));
  }
}
