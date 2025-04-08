package sns.pinocchio.presentation.post;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import sns.pinocchio.application.post.PostCreateRequest;
import sns.pinocchio.application.post.PostModifyRequest;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;

@Tag(name = "게시글", description = "게시글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Operation(
            summary = "게시글 생성",
            description = "게시글을 작성합니다. 해시태그는 자동 저장됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류 또는 누락된 필드"),
            @ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
            // create 에는 404 필요없음
    })
    @PostMapping
    public ResponseEntity<String> createPost(
            @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String tsid = userDetails.getTsid();
        String postId = postService.createPost(request, tsid);
        return ResponseEntity.ok(postId);
    }

    @Operation(
            summary = "게시글 수정",
            description = "기존 게시글의 본문, 이미지, 공개여부를 수정합니다."
    )
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
            @RequestBody PostModifyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.modifyPost(request, userDetails.getTsid());
        return ResponseEntity.ok("게시글이 수정되었습니다.");
    }

    @Operation(
            summary = "게시글 삭제",
            description = "작성자가 자신의 게시글을 소프트 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
            @ApiResponse(responseCode = "403", description = "작성자 본인만 삭제 가능"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails.getTsid());
        return ResponseEntity.ok("게시글이 삭제되었습니다.");
    }

    @Operation(
            summary = "게시글 상세 조회",
            description = "특정 게시글의 상세 정보를 조회합니다. 공개글은 누구나 볼 수 있고, 비공개 게시물은 작성자만 볼 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "403", description = "비공개 게시물 접근 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음 or 삭제됨"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String tsid = (userDetails != null) ? userDetails.getTsid() : null;
        return ResponseEntity.ok(postService.getPostDetail(postId, tsid));
    }

}