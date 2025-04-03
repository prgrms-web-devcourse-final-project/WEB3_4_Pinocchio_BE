package sns.pinocchio.config.global.initData;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import sns.pinocchio.application.member.memberDto.request.SignupRequestDto;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai")
public class AiUserProperties {
	private List<SignupRequestDto> users;
}
