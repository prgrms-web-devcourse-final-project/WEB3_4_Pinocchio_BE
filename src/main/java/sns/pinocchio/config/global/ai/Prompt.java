package sns.pinocchio.config.global.ai;

public class Prompt {
	private String memberPost;
	private String answerEx = "참고:";
	private String basePrompt;

	void setMemberPost(String memberPost) {
		this.memberPost = "작성글:" + memberPost;
	}

	void addAnswerEx(String answerEx) {
		this.answerEx += answerEx + ",";
	}

	void setBasePrompt(String prompt) {
		this.basePrompt = "해당 내용을 따라야한다:" + prompt;
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
