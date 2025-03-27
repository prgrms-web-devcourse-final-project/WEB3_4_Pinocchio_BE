package sns.pinocchio.application.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//댓글 삭제 요청 DTO
@Data
@Builder
public class CommentDeleteRequest {
	@Schema(description = "삭제할 댓글의 postId")
	String postId;
	@Schema(description = "삭제할 commendId")
	String commentId;
	@Schema(description = "삭제유형: SOFT_DELETED,HARD_DELETED")
	DeleteType action;
}
