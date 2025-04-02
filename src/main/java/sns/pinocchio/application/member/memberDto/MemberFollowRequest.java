package sns.pinocchio.application.member.memberDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

//유저 팔로잉 요청 DTO
@Data
@Builder
public class MemberFollowRequest {
	@Schema(description = "팔로잉 닉네임")
	String followingNickname;
}
