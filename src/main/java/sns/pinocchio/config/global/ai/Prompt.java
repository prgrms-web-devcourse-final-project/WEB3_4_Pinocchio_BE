package sns.pinocchio.config.global.ai;

import java.util.List;
import java.util.Comparator;
import lombok.Getter;
import lombok.Setter;
import sns.pinocchio.infrastructure.ai.vectorDB.VectorQuery;

@Getter
@Setter
public class Prompt {
	private String memberPost;
	private List<VectorQuery.SimilarityResult> answerEx;
	private List<String> basePrompt;


	@Override
	public String toString() {
		return String.format(
			"게시글:%s, 주어진대사:[%s], %s",
			memberPost,
			getTopAnswer(),
			basePrompt
		).trim();
	}

	public String getTopAnswer() {
		String topAnswer = "";
		if (answerEx != null && !answerEx.isEmpty()) {
			topAnswer = answerEx.stream()
				.max(Comparator.comparingDouble(VectorQuery.SimilarityResult::getScore))
				.map(VectorQuery.SimilarityResult::getUtterance)
				.orElse("");
		}
		return topAnswer;
	}
}
