package sns.pinocchio.domain.comment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sns.pinocchio.config.global.enums.CancellState;

@Document(collection = "commentlikes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {
	@Id
	private String id;  // MongoDB의 _id 필드

	private String userId;  // 작성자 ID

	private String commentId;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private CancellState status;

	public boolean toggleCommentLike(){
		if(this.getStatus() == CancellState.ACTIVE){
			this.setStatus(CancellState.CANCELLED);
			return false;
		} else{
			this.setStatus(CancellState.ACTIVE);
			return true;
		}
	}
}
