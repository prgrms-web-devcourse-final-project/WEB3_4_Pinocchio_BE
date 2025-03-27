package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

//댓글 수정 요청 DTO
@Data
@Builder
public class CommentModifyRequest {
	String userId;
	String postId;     // 게시글 ID
	String commentId;  // 댓글 ID
	String content;  // 수정할 댓글 내용
}