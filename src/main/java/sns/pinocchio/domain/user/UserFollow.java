package sns.pinocchio.domain.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sns.pinocchio.config.global.enums.CancellState;

@Document(collection = "follows")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollow {
	@Id
	private String id;  // MongoDB의 _id 필드

	private String followerId;

	private String followerNickname;

	private String followingId;

	private String followingNickname;

	private CancellState status;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	public boolean toggleFollowStatus() {
		if (this.status == CancellState.ACTIVE) {
			this.status = CancellState.CANCELLED;
			return false;
		} else{
			this.status =CancellState.ACTIVE;
			return true;
		}
	}

}
