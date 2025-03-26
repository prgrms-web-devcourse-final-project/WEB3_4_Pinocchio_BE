package sns.pinocchio.application.comment;

import lombok.Builder;
import lombok.Data;

//댓글 좋아요 요청 DTO
@Data
@Builder
public class CommentLikeRequest {
	String postId;
}
