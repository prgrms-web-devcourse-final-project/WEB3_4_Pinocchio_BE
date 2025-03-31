package sns.pinocchio.application.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder// 테스트를 위한 빌더
//(api 테스트시 postId를 따로 넣어야 할것)
public class PostModifyRequest {
    @Schema(hidden = true)
    private String postId;

    @Schema(hidden = true)
    private String tsid;

    @Schema(description = "수정할 본문 내용", example = "수정된 게시글입니다.")
    private String content;

    @Schema(description = "수정할 이미지 URL 리스트", example = "[\"https://img.com/updated.jpg\"]")
    private List<String> imageUrls;

    @Schema(description = "공개 범위 (public/private)", example = "private")
    private String visibility;
}