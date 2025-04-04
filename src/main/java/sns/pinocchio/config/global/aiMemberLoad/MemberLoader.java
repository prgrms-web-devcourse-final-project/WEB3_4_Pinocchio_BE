package sns.pinocchio.config.global.aiMemberLoad;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sns.pinocchio.application.member.MemberService;
import sns.pinocchio.domain.member.Member;
import sns.pinocchio.presentation.member.exception.MemberException;

@Slf4j
@Component
public class MemberLoader {
	private final MemberService memberService;

	@Getter
	private List<AiMember> memberList = new ArrayList<>(); // 기본값 설정

	public MemberLoader(MemberService memberService) {
		this.memberService = memberService;
		loadMembers();
	}

	// AI 유저 정보 불러오기
	private void loadMembers() {
		ObjectMapper objectMapper = new ObjectMapper();
		ClassPathResource resource = new ClassPathResource("aiMember.json");

		try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
			JsonNode rootNode = objectMapper.readTree(reader);
			JsonNode membersNode = rootNode.get("members");

			if (membersNode != null && membersNode.isArray()) {
				memberList = new ArrayList<>();

				for (JsonNode memberNode : membersNode) {
					AiMember aiMember = parseAiMember(memberNode);
					Member member = createMember(aiMember);
					aiMember.setMember(member);
					memberList.add(aiMember);
				}
			}
		} catch (IOException e) {
			log.error("ai member insert error:", e);
		}
	}

	// JSON에서 AiMember 불러오기
	private AiMember parseAiMember(JsonNode memberNode) {
		return AiMember.builder()
			.name(memberNode.get("name").asText())
			.email(memberNode.get("email").asText())
			.nickname(memberNode.get("nickname").asText())
			.password(memberNode.get("password").asText())
			.prompt(memberNode.get("prompt").asText())
			.type(memberNode.get("type").asText())
			.build();
	}

	// AI 유저 Member 변환
	private Member createMember(AiMember aiMember) {
		try {
			return memberService.findByEmail(aiMember.getEmail()); // 기존 멤버 반환
		} catch (MemberException e) {
			return memberService.createMember(aiMember.toSignupRequestDto());
		}
	}
}