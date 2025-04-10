package sns.pinocchio.presentation.post;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sns.pinocchio.application.post.PostLikeService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;

@Tag(name = "게시글 좋아요", description = "게시글 좋아요/취소 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/like")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(
            summary = "게시글 좋아요 토글",
            description = "게시글에 좋아요 또는 좋아요 취소를 합니다.",
            security = @SecurityRequirement(name = "bearerAuth") // Swagger용 인증 표시
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 토글 처리됨"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "자기 자신의 게시글에는 좋아요 불가"),
            @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{postId}/toggle")
    public ResponseEntity<String> toggleLike(
            @PathVariable String postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String tsid = userDetails.getTsid(); // 인증된 사용자 TSID 추출
        postLikeService.toggleLike(postId, tsid);
        return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
    }

}
