package sns.pinocchio.application.post;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder// 테스트를 위해서 빌드 추가
@Data
public class PostCreateRequest {
    private String content;
    private List<String> imageUrls;
    private List<String> hashtags;
    private List<String> mentions;
    private String visibility; // public | private
}