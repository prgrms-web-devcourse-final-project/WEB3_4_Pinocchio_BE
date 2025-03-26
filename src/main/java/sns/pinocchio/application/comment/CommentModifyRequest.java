package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentModifyRequest {
	String postId;     // 게시글 ID
	String commentId;  // 댓글 ID
	String content;  // 수정할 댓글 내용
}