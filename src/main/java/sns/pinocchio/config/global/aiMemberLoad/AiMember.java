package sns.pinocchio.config.global.aiMemberLoad;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;
import sns.pinocchio.domain.member.Member;

@Builder
@Getter
public class AiMember {
	private String name;
	private String email;
	private String nickname;
	private String password;
	private String prompt;
	@Setter
	private Member member;

 	public SignupRequestDto toSignupRequestDto(){
		 return new SignupRequestDto(name,email,nickname,password);
	}
}