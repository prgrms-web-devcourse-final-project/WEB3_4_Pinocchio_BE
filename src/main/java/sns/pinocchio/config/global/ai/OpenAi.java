package sns.pinocchio.config.global.ai;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.Options;
import io.github.ollama4j.utils.OptionsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sns.pinocchio.config.global.aiMemberLoad.AiMember;
import sns.pinocchio.infrastructure.ai.VectorQuery;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenAi {
	private final OpenAiChatModel chatClient;

	public String getAnswer(String prompt) {
		Options option = new OptionsBuilder().build();
			String result = chatClient.call(prompt);
			return result;
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
