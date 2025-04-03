package sns.pinocchio.infrastructure.ai.ollama;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;

import java.io.IOException;

public class OllamaChatService {
  private final OllamaAPI ollamaApi;
  private final String modelName;


  //Ollama API 서버의 기본 URL (예: "http://localhost:11434")
  public OllamaChatService(String ollamaBaseUrl, String modelName) {

    this.ollamaApi = new OllamaAPI(ollamaBaseUrl);
    // this.ollamaApi.setReadTimeout(60); // 필요시 타임아웃(초) 설정
    this.modelName = modelName;
//    System.out.println("초기화: " + ollamaBaseUrl + ", 모델명: " + modelName);
  }

  public String getAiCharacterResponse(String utterance, String characterDescription) {
    if (utterance == null || utterance.isBlank()) {
      System.err.println("Utterance가 null이거나 빈 값 입니다.");
      return null;
    }

    try {
      // --- 프롬프트 설정 ---
      String systemPrompt = "당신은 한국의 영화 배우 이경영입니다. 이경영이 연기한 캐릭터들의 말투와 성격을 반영하여 댓글을 작성하세요. 방금 주어진 대사를 듣거나 말한 상황이라고 가정하고, 그 상황과 당신의 캐릭터 성격에 맞춰 자연스럽게 다음 대사를 이어나가세요. 악역 대사를 사용해주세요. 검색된 대사를 참고하여 자연스럽게 응답하세요. 대사를 그대로 복사하지 말고 상황에 맞게 변형하여 활용하세요. SNS 댓글 형식으로 작성하세요. 사용자의 게시물을 요약하지 마세요. 너무 길지 않게, 실제 대화처럼 응답해야 합니다.";
      String userPrompt = "주어진 대사: \"" + utterance + "\"";

      // Ollama 채팅 요청 생성
      OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(this.modelName)
              .withMessage(OllamaChatMessageRole.SYSTEM, systemPrompt)
              .withMessage(OllamaChatMessageRole.USER, userPrompt);

      OllamaChatRequest request = builder.build();

      //System.out.println("Sending request to Ollama model: " + this.modelName);
      // System.out.println("System Prompt: " + systemPrompt); // 디버깅 시 프롬프트 확인
      // System.out.println("User Prompt: " + userPrompt);

      OllamaChatResult result = this.ollamaApi.chat(request);

      if (result != null && result.getResponse() != null && !result.getResponse().isBlank()) {

        if (result.toString() != null && result.toString() != null) {
          return result.toString().trim();
        } else {

          return result.getResponse().trim();
        }
      } else {
        System.err.println("Received empty or null response from Ollama.");
        return null;
      }

    } catch (OllamaBaseException e) {

      System.err.println("Ollama API error: " + e.getMessage());
      e.printStackTrace();
      return "죄송합니다, 응답을 생성하는 중 오류가 발생했습니다. (Ollama Error)";
    } catch (IOException | InterruptedException e) {

      System.err.println("Error communicating with Ollama: " + e.getMessage());
      e.printStackTrace();
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return "죄송합니다, 응답을 생성하는 중 통신 오류가 발생했습니다.";
    } catch (Exception e) {

      System.err.println("An unexpected error occurred: " + e.getMessage());
      e.printStackTrace();
      return "죄송합니다, 알 수 없는 오류가 발생했습니다.";
    }
  }

  public String getAiCharacterResponse(String utterance) {
    return getAiCharacterResponse(utterance, null);
  }


  public static void main(String[] args) {

    //환경변수 처리 필요
    String ollamaUrl = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434");
    String model = System.getenv().getOrDefault("OLLAMA_MODEL", "llama3.1"); // 사용할 모델 이름

    OllamaChatService chatService = new OllamaChatService(ollamaUrl, model);

    String testUtterance = "의원님 큰일 났습니다!";

    System.out.println("Input Utterance: " + testUtterance);

    String aiResponse = chatService.getAiCharacterResponse(testUtterance);

    System.out.println("\nAI Response:");
    if (aiResponse != null) {
      System.out.println(aiResponse);
    } else {
      System.out.println("Failed to get response.");
    }
  }
}
