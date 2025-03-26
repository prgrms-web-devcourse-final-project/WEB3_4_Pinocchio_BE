package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentModifyRequest {
	private long postId;     // 게시글 ID
	private long commentId;  // 댓글 ID
	private String content;  // 수정할 댓글 내용
}