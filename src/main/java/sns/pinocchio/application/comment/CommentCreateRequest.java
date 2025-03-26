package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

//댓글 생성 요청 DTO
@Data
@Builder
public class CommentCreateRequest {
	String userId;
	String content;
	String parentCommentId;
}
