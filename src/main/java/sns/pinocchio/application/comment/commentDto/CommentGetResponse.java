package sns.pinocchio.application.comment.commentDto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import sns.pinocchio.config.global.enums.CancellState;

@Data
@Builder
public class CommentGetResponse {
	String id;
	String userId;
	String postId;
	String content;
	String parentCommentId;
	int likes;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
	CancellState state;
	boolean liked;
}
