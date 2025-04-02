package sns.pinocchio.application.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//댓글 생성 요청 DTO
@Data
@Builder
public class CommentCreateRequest {
	@Schema(description = "댓글의 게시글 postID")
	String postId;
	@Schema(description = "댓글 내용")
	String content;
	@Schema(description = "댓글 부모 commentId")
	String parentCommentId;
}
