package sns.pinocchio.config.global.ollama;

public class Prompt {
	private String memberPost;
	private String answerEx = "anwer Ex:";
	private String basePrompt;

	void setMemberPost(String memberPost) {
		this.memberPost = "Member Post:" + memberPost;
	}

	void addAnswerEx(String answerEx) {
		this.answerEx += answerEx + ",";
	}

	void setBasePrompt(String prompt) {
		this.basePrompt = "Your writing format:" + prompt;
	}

	@Override
	public String toString() {
		return String.format(
			"%s %s %s",
			memberPost,
			answerEx,
			basePrompt
		).trim();
	}
}
