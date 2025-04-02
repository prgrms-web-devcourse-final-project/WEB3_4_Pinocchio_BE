package sns.pinocchio.application.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//댓글 수정 요청 DTO
@Data
@Builder
public class CommentModifyRequest {
	@Schema(description = "댓글의 postId")
	String postId;
	@Schema(description = "댓글 commentId")
	String commentId;
	@Schema(description = "수정할 내용",example = "스웨거로 수정했습니다.")
	String content;
}