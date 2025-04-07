package sns.pinocchio.presentation.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.post.PostLikeService;

@Tag(name = "게시글 좋아요", description = "게시글 좋아요/취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/like")
public class PostLikeController {

  private final PostLikeService postLikeService;

  @Operation(summary = "게시글 좋아요 토글", description = "게시글에 좋아요 또는 좋아요 취소를 합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "성공적으로 토글 처리됨"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
    @ApiResponse(responseCode = "401", description = "인증 실패"),
    @ApiResponse(responseCode = "403", description = "자기 자신의 게시글에는 좋아요 불가"),
    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/{postId}")
  public ResponseEntity<String> toggleLike(
      @PathVariable String postId,
      @RequestHeader("Authorization") String accessToken // JWT 토큰 (현재는 사용 안 함)
      ) {
    String userTsid = "mockTsid"; // TODO: JWT에서 추출 예정

    postLikeService.toggleLike(postId, userTsid);
    return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
  }
}
