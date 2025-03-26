package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDeleteRequest {
	String postId;
	String commentId;
	DeleteType action;
}
