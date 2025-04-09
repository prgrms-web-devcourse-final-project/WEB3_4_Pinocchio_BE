package sns.pinocchio.infrastructure.ai.ollama;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.models.chat.OllamaChatResult;

import sns.pinocchio.infrastructure.ai.vectorDB.VectorQuery;
import sns.pinocchio.infrastructure.ai.vectorDB.VectorQuery.SimilarityResult;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class OllamaChatService {
  private final OllamaAPI ollamaApi;
  private final String modelName;


  //Ollama API 서버의 기본 URL (예: "http://localhost:11434")
  public OllamaChatService(String ollamaBaseUrl, String modelName) {

    this.ollamaApi = new OllamaAPI(ollamaBaseUrl);
    this.modelName = modelName;
  }

  public String getAiCharacterResponse(String utterance, int vectorSearchLimit) {
    if (utterance == null || utterance.isBlank()) {
      System.err.println("Utterance가 null이거나 빈 값 입니다.");
      return null;
    }

    // 백터 쿼리로 유사 문서 검색
    try {
      List<SimilarityResult> similarDocuments =
              VectorQuery.searchSimilarDocuments(utterance, vectorSearchLimit);

      // 검색 결과가 없을 경우 처리
      String relatedUtterance = null;
      if (similarDocuments.isEmpty()) {
        System.out.println("VectorQuery 검색 결과가 없습니다.");
      } else {
        // 가장 높은 score를 가진 문서 선택
        Optional<SimilarityResult> topResult = similarDocuments.stream()
                .max((doc1, doc2) -> Double.compare(doc1.getScore(), doc2.getScore()));

        if (topResult.isPresent()) {
          relatedUtterance = topResult.get().getUtterance();
          System.out.println("가장 높은 점수의 검색 결과:");
          System.out.println(" - Utterance: " + relatedUtterance);
          System.out.println(" - Score: " + topResult.get().getScore());
        }
      }

      // --- 프롬프트 설정 ---
      String systemPrompt = """
        당신은 한국의 영화 배우 이경영입니다. 이경영이 연기한 캐릭터들의 말투와 성격을 반영하여 SNS 댓글을 작성하세요. 
        악역 대사를 사용하고, 상황에 맞게 자연스럽게 변형하여 활용하세요. 
        단, 주어진 대사가 [졸라 고독하구만, 닥쳐! 이 병신새끼야, 니가 싼 똥이니까 니가 치워, 자아~ 갑니다!] 중 하나라면, 대사를 변형하지 말고 그대로 출력하세요. 
        지시문은 생략하고, 너무 길지 않게 실제 대화처럼 응답해야 합니다.
        """;

      String userPrompt = "주어진 대사: \"" + utterance + "\"";

      // Ollama 채팅 요청 생성
      OllamaChatRequestBuilder builder = OllamaChatRequestBuilder.getInstance(this.modelName)
              .withMessage(OllamaChatMessageRole.SYSTEM, systemPrompt)
              .withMessage(OllamaChatMessageRole.USER, userPrompt);

      OllamaChatRequest request = builder.build();

      //System.out.println("Sending request to Ollama model: " + this.modelName);
      // System.out.println("System Prompt: " + systemPrompt); // 디버깅 시 프롬프트 확인
      // System.out.println("User Prompt: " + userPrompt);

      // Ollama API 호출
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


  public static void main(String[] args) {

    //환경변수 처리 필요
    String ollamaUrl = System.getenv().getOrDefault("OLLAMA_BASE_URL", "http://localhost:11434");
    String model = System.getenv().getOrDefault("OLLAMA_MODEL", "gemma3:1b"); // 사용할 모델 이름

    OllamaChatService chatService = new OllamaChatService(ollamaUrl, model);

    String testUtterance = "화가 많이 나셨어요?";
    int vectorSearchLimit = 10;

    System.out.println("Input Utterance: " + testUtterance);

    String aiResponse = chatService.getAiCharacterResponse(testUtterance, vectorSearchLimit);

    System.out.println("\nAI Response:");
    if (aiResponse != null) {
      System.out.println(aiResponse);
    } else {
      System.out.println("Failed to get response.");
    }
  }
}
