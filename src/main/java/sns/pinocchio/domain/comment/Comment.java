package sns.pinocchio.domain.comment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sns.pinocchio.config.global.enums.CancellState;

@Document(collection = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
	@Id
	private String id;  // MongoDB의 _id 필드

	@Indexed
	private String userId;  // 작성자 ID

	private String postId;  // 게시글 ID

	private String content; // 댓글 내용

	private String parentCommentId; // 대댓글인 경우 부모 댓글 ID

	private int likes; // 좋아요 수 (캐시용)

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private CancellState status; // active / deleted (Soft Delete)

	public int updateLikes(boolean isLiked){
		if(isLiked){
			this.likes = this.likes +1;
		} else{
			this.likes = this.likes - 1;
		}
		return this.likes;
	}
}
