package sns.pinocchio.config.global.ai;

public class Prompt {
	private String memberPost ="";
	private String answerEx ="";
	private String basePrompt ="";

	void setMemberPost(String memberPost) {
		this.memberPost = memberPost;
	}

	void addAnswerEx(String answerEx) {
		this.answerEx += answerEx + ",";
	}

	void setBasePrompt(String prompt) {
		this.basePrompt =  prompt;
	}

	@Override
	public String toString() {
		return String.format(
			"게시글:%s, 주어진대사:[%s], 지침:[%s]",
			memberPost,
			answerEx,
			basePrompt
		).trim();
	}
}
