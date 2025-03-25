package sns.pinocchio.domain.post;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    private String id;  // _id

    @Indexed
    private String userId;  // 게시글 작성자 ID

    private String content;  // 게시글 내용

    private List<String> imageUrls;  // 이미지 URL 리스트

    private List<String> hashtags;  // 해시태그 목록 (ex. ["#여행", "#제주도"])

    private int likes;  // 좋아요 수 (캐시용)

    private int commentsCount;  // 댓글 수 (캐시용)

    private int views;  // 조회수

    private String visibility;  // 공개 여부: "public" or "private"

    private List<String> mentions;  // 언급된 사용자 ID 리스트

    private String status;  // "active" or "deleted" (소프트 딜리트용)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}