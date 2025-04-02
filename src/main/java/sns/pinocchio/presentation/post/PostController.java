package sns.pinocchio.presentation.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.post.PostCreateRequest;
import sns.pinocchio.application.post.PostModifyRequest;
import sns.pinocchio.application.post.PostService;

@Tag(name = "게시글", description = "게시글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

  private final PostService postService;

  @Operation(summary = "게시글 생성", description = "게시글을 작성합니다. 해시태그는 자동 저장됩니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "게시글 생성 성공"),
    @ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 누락된 필드"),
    @ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    // create 에는 404 필요없음
  })
  @PostMapping("/create")
  public ResponseEntity<String> createPost(
      @RequestBody PostCreateRequest request,
      @RequestHeader("Authorization") String accessToken // JWT 파싱 예정
      ) {
    String userId = "mockUser"; // 임시 값, 나중에 JWT에서 추출
    String postId = postService.createPost(request, userId);
    return ResponseEntity.ok(postId);
  }

  @Operation(summary = "게시글 수정", description = "기존 게시글의 본문, 이미지, 공개여부를 수정합니다.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
    @ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
    @ApiResponse(responseCode = "403", description = "작성자 본인만 수정 가능"),
    @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/modify")
  public ResponseEntity<String> modifyPost(
      @RequestBody PostModifyRequest request, @RequestHeader("Authorization") String accessToken) {
    String userId = "mockUser"; // TODO: JWT에서 파싱 예정

    postService.modifyPost(request, userId);
    return ResponseEntity.ok("게시글이 수정되었습니다.");
  }
}
