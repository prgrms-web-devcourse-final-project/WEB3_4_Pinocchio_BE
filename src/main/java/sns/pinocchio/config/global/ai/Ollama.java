package sns.pinocchio.config.global.ai;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.Options;
import io.github.ollama4j.utils.OptionsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sns.pinocchio.config.global.aiMemberLoad.AiMember;
import sns.pinocchio.infrastructure.ai.vectorDB.VectorQuery;

@Slf4j
@RequiredArgsConstructor
@Component
public class Ollama {
	private final OllamaAPI ollamaAPI;
	private final String model;
	private final String host;

	@Autowired
	public Ollama(@Value("${ollama.model}") String model, @Value("${ollama.host}") String host) {
		this.model = model;
		this.host = host;
		this.ollamaAPI = new OllamaAPI(host);
	}

	public String getAnswer(String prompt) {
		Options option = new OptionsBuilder().build();
		try {
			OllamaResult result = ollamaAPI.generate("gemma3:1b", prompt, false, option);
			return result.getResponse();
		} catch (OllamaBaseException | IOException | InterruptedException e) {
			log.error("ollama API error:", e);
			return "";
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
