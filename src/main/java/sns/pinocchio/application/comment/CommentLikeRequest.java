package sns.pinocchio.application.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//댓글 좋아요 요청 DTO
@Data
@Builder
public class CommentLikeRequest {
	@Schema(description = "좋아요할 댓글의 postId")
	String postId;
}
