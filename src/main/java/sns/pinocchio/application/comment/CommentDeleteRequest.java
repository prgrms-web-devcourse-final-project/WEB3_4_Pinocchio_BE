package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

//댓글 삭제 요청 DTO
@Data
@Builder
public class CommentDeleteRequest {
	String postId;
	String commentId;
	DeleteType action;
}
