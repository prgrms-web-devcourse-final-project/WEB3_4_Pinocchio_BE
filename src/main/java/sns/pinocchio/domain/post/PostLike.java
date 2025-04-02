package sns.pinocchio.domain.post;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "post_likes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    private String id;  // MongoDB ObjectId or TSID ("like_01226N...")

    @Indexed
    private String postId;  // 게시글의 MongoDB ObjectId

    @Indexed
    private String postTsid;  // 게시글 작성자의 TSID (자기 글 좋아요 방지용)

    @Indexed
    private String tsid;  // 좋아요를 누른 사람의 TSID

    private LikeStatus status; // enum 타입 관리 "active" or "cancelled" (소프트 딜리트 대응)

    private LocalDateTime likedAt;     // 최초 좋아요 누른 시간

    private LocalDateTime updatedAt;   // 최근 토글 시간 (누름/취소 시점)
}