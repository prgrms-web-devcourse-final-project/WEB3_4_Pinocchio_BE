package sns.pinocchio.config.global.ollama;

public class Prompt {
	private String memberPost;
	private String memberNickname;
	private String answerEx = "anwer Ex:";
	private String basePrompt;
	void setMemberPost(String memberPost){
		this.memberPost +="Member Post:"+memberPost;
	}
	void setMememberNickname(String mememberNickname){
		this.memberNickname +="Member Nickname:"+mememberNickname;
	}
	void addAnswerEx(String answerEx){
		this.answerEx += answerEx +",";
	}
	void setBasePrompt(String prompt){
		this.basePrompt += "Your writing format:" +prompt;
	}
	@Override
	public String toString() {
		return String.format(
			"%s %s %s %s",
			memberPost,
			memberNickname,
			answerEx,
			basePrompt
		).trim();
	}
}
