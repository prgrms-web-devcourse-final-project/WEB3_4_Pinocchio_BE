package sns.pinocchio.presentation.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sns.pinocchio.application.post.PostCreateRequest;
import sns.pinocchio.application.post.PostModifyRequest;
import sns.pinocchio.application.post.PostSearchService;
import sns.pinocchio.application.post.PostService;
import sns.pinocchio.config.global.auth.model.CustomUserDetails;

import java.io.IOException;

@Tag(name = "게시글", description = "게시글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;
    private final PostSearchService postSearchService;

    @PostMapping(value = "/swagger", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 생성 (Swagger 전용)", description = "Swagger에서 테스트용으로 사용되는 엔드포인트입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<String> createPostForSwagger(
            @RequestPart("request") String requestJson,  // JSON 문자열 직접 파싱
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        PostCreateRequest request = objectMapper.readValue(requestJson, PostCreateRequest.class);

        String tsid = userDetails.getTsid();
        String postId = postService.createPostWithImage(request, image, tsid);
        return ResponseEntity.ok(postId);
    }


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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPost(
            @RequestPart("request") PostCreateRequest request,
            @RequestPart("image") MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        String tsid = userDetails.getTsid();
        String postId = postService.createPostWithImage(request, image, tsid);
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
            description = """
        작성자가 자신의 게시글을 삭제하거나 비공개로 전환합니다.
        <br><br>
        - `action=delete` : 소프트 삭제 (status = deleted + 비공개 전환)
        <br>
        - `action=private` : 공개 → 비공개 전환 (status 유지)
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 처리 성공"),
            @ApiResponse(responseCode = "400", description = "지원하지 않는 삭제 유형"),
            @ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 인증 실패"),
            @ApiResponse(responseCode = "403", description = "작성자 본인만 삭제 가능"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "delete") String action,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        postService.deletePost(postId, userDetails.getTsid(), action);
        return ResponseEntity.ok("게시글 처리 완료: " + action);
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
    public ResponseEntity<?> getPostDetail(@PathVariable String postId) {
        String tsid = null;

        // SecurityContext에서 인증 정보 수동 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            tsid = userDetails.getTsid();
        }

        return ResponseEntity.ok(postService.getPostDetail(postId, tsid));
    }

    @Operation(
            summary = "게시글 검색",
            description = "검색어에 해당하는 게시글을 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 검색 성공"),
            @ApiResponse(responseCode = "400", description = "커서의 날짜 형식이 올바르지 않을 경우"),
            @ApiResponse(responseCode = "403", description = "게시글 검색에 대한 권한이 없을 경우"),
            @ApiResponse(responseCode = "404", description = "검색할 유저 정보를 찾을 수 없을 경우"),
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "type", required = false, defaultValue = "posts") String type,
            @RequestParam(name = "limit", required = false, defaultValue = "9") int limit,
            @RequestParam(name = "sortBy", required = false, defaultValue = "latest") String sortBy,
            @RequestParam(name = "cursor", required = false) String cursor
    ) {
        return ResponseEntity.ok(
                postSearchService.searchPosts(userDetails, query, type, sortBy, limit, cursor));
    }
}