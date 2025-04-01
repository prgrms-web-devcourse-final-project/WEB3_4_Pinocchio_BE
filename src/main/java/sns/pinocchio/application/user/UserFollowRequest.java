package sns.pinocchio.application.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//유저 팔로잉 요청 DTO
@Data
@Builder
public class UserFollowRequest {
	@Schema(description = "팔로잉 닉네임")
	String followingNickname;
}
