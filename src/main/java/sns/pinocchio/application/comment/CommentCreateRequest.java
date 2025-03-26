package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentCreateRequest {
	String userId;
	String content;
	String parentCommentId;
}
