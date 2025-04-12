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
            summary = "게시글 좋아요 누르기", // ✅ 설명 변경
            description = "게시글에 좋아요를 누릅니다. 누를 때마다 좋아요 수가 1씩 증가합니다.", // ✅ 무한 누적 좋아요 설명으로 변경
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공적으로 좋아요가 처리됨"), // ✅ 응답 메시지도 변경
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{postId}")
    public ResponseEntity<String> like( // ✅ toggleLike → like 이름 변경
                                        @PathVariable String postId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String tsid = userDetails.getTsid();
        postLikeService.like(postId, tsid); // ✅ 새로운 like() 호출
        return ResponseEntity.ok("좋아요가 추가되었습니다."); // ✅ 응답 메시지도 변경
    }
}