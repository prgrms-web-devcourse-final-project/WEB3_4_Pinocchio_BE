package sns.pinocchio.application.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder// 테스트를 위해서 빌드 추가
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @Schema(description = "게시글 내용")
    private String content;

    @Schema(description = "이미지 URL 리스트")
    private List<String> imageUrls;

    @Schema(description = "해시태그 리스트 (예: [\"#여행\", \"#제주도\"])")
    private List<String> hashtags;

    @Schema(description = "멘션된 사용자 ID 리스트")
    private List<String> mentions;

    @Schema(description = "공개 여부: public | private")
    private String visibility;
}