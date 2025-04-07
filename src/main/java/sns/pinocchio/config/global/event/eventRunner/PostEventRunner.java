package sns.pinocchio.config.global.event.eventRunner;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import sns.pinocchio.application.comment.CommentService;
import sns.pinocchio.application.comment.commentDto.CommentCreateRequest;
import sns.pinocchio.config.global.aiMemberLoad.AiMember;
import sns.pinocchio.config.global.aiMemberLoad.MemberLoader;
import sns.pinocchio.config.global.event.PostEvent;
import sns.pinocchio.config.global.ollama.Ollama;
import sns.pinocchio.domain.member.Member;

@Component
@RequiredArgsConstructor
public class PostEventRunner {
	private final MemberLoader memberLoader;
	private final CommentService commentService;
	private final Ollama ollama;

	public void createAiComment(PostEvent event) {
		List<AiMember> aiMemberList = memberLoader.getMemberList();
		for (AiMember aiMember : aiMemberList) {
			Member member = aiMember.getMember();
			String prompt = ollama.convertPrompt(event.getContent(), aiMember);
			String answer = ollama.getAnswer(prompt);
			if (!Objects.equals(answer, "")) {
				CommentCreateRequest request = CommentCreateRequest.builder()
					.content(answer)
					.postId(event.getPostId())
					.build();
				commentService.createComment(request, member.getTsid());
			}
		}
	}

}
