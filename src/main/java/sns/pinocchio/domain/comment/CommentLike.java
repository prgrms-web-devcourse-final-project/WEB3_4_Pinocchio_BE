package sns.pinocchio.domain.comment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "commentlikes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentLike {
	@Id
	private String id;  // MongoDB의 _id 필드

	@Indexed
	private String userId;  // 작성자 ID

	private String commentId;

	private LocalDateTime createdAt;
}
