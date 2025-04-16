package sns.pinocchio.application.comment.commentDto;

import lombok.Builder;
import lombok.Data;
import sns.pinocchio.config.global.enums.CancellState;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentGetResponse {
	String id;
	String userId;
    String nickname;
    String postId;
	String content;
	String parentCommentId;
	int likes;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
	CancellState state;
	boolean liked;
}
