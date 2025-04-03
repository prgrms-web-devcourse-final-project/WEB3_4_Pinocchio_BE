package sns.pinocchio.config.global.initData;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;


@Configuration
@RequiredArgsConstructor
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public class InitData { // 클래스에 public 추가
	private final AiUserProperties aiUserProperties;
	private final MemberService memberService;


	@Bean
	public ApplicationRunner baseInitDataApplicationRunner() {
		return args -> {
			List<SignupRequestDto> users = aiUserProperties.getUsers();
			for (SignupRequestDto requestDto : users) {
				try {
					memberService.createMember(requestDto);
				} catch (Exception e) {
					System.out.println("등록된 사용자: " + requestDto.getEmail());
				}
			}
		};
	}
}