package sns.pinocchio.application.post;

import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private String content;
    private List<String> imageUrls;
    private List<String> hashtags;
    private List<String> mentions;
    private String visibility; // public | private
}