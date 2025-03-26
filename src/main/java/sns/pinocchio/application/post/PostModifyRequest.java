package sns.pinocchio.application.post;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostModifyRequest {
    private String postId;
    private String userId; // 작성자
    private String content;
    private List<String> imageUrls;
    private String visibility; // "public" or "private"
}