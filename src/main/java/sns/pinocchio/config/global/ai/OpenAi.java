package sns.pinocchio.config.global.ai;

import java.util.List;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.ollama4j.utils.Options;
import io.github.ollama4j.utils.OptionsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sns.pinocchio.config.global.aiMemberLoad.AiMember;
import sns.pinocchio.infrastructure.ai.vectorDB.VectorQuery;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenAi {
	private final OpenAiChatModel chatClient;

	@Value("${ai.response.fail.delayTime}")
	private int delay;

	public String getAnswer(String prompt, int retryCount) throws InterruptedException {
		try {
			return chatClient.call(prompt);
		} catch (NonTransientAiException e) {
			if (retryCount >= 10) {
				return "";
			}
			Thread.sleep(delay * 1000L);
			return getAnswer(prompt, retryCount + 1);
		}
	}

	public String convertPrompt(String content, AiMember aiMember) {
		Prompt prompt = new Prompt();
		prompt.setMemberPost(content);
		List<VectorQuery.SimilarityResult> results = VectorQuery.searchSimilarDocuments(content, 5);
		for (VectorQuery.SimilarityResult r : results) {
			prompt.addAnswerEx(r.getUtterance());
		}
		prompt.setBasePrompt(aiMember.getPrompt());
		return prompt.toString();
	}
}
