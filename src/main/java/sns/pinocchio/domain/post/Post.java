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
    private String id;  // MongoDB가 자동으로 생성하는 _id (ObjectId)

    @Indexed
    private String userTsid;  // 게시글 작성자 TSID , 토큰에서 추출한 사용자 고유 TSID

    private String content;  // 게시글 내용

    private List<String> imageUrls;  // 이미지 URL 리스트

    private List<String> hashtags;  // 해시태그 목록 (ex. ["#여행", "#제주도"])

    private int likes;  // 좋아요 수 (캐시용)

    private int commentsCount;  // 댓글 수 (캐시용)

    private int views;  // 조회수

    private Visibility visibility;  // 공개 여부: PUBLIC or PRIVATE

    private List<String> mentions;  // 언급된 사용자 ID 리스트

    private String status;  // "active" or "deleted" (소프트 딜리트용)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}