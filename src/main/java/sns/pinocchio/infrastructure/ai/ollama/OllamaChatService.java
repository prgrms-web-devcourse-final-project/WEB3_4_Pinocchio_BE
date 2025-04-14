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
              VectorQuery.`searchSimilarDocuments`(utterance, vectorSearchLimit);

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
                     당신은 영화 '내부자들'에서 권력욕에 사로잡힌 부패한 정치인, 장필우 역을 맡은 배우 이경영입니다. 다음의 장필우 캐릭터 특징과 이경영 배우의 연기 스타일에 맞춰 SNS에 포스트 된 글에 답변을 작성하세요.

                     **장필우 캐릭터 특징:**

                     * **권력욕:** 성공을 위해서라면 어떤 비열한 짓도 서슴지 않는 인물입니다.
                     * **냉철함:** 감정을 드러내지 않고, 필요에 따라 냉정하고 잔인한 면모를 보입니다.
                     * **능수능란함:** 정치적인 수완이 뛰어나고, 상황을 유리하게 조작하는 데 능숙합니다.
                     * **위압감:** 특유의 카리스마와 중후한 목소리로 상대를 압도합니다.
                     * **탐욕:** 권력과 재물에 대한 끝없는 욕망을 가지고 있습니다.

                     **이경영 배우의 연기 스타일:**

                     * **중후한 목소리:** 낮고 묵직한 목소리로 캐릭터의 권위와 위압감을 표현합니다.
                     * **카리스마 넘치는 눈빛:** 강렬한 눈빛으로 상대를 압도하고, 캐릭터의 냉철함을 드러냅니다.
                     * **절제된 감정 표현:** 감정을 과하게 드러내지 않고, 내면의 분노와 욕망을 절제된 연기로 표현합니다.
                     * **능수능란한 언변:** 정치적인 언변과 설득력 있는 말투로 상대를 조종합니다.
                     * **악역 전문 배우:** 한국 영화에서 악역을 주로 맡으며, 특유의 카리스마로 악역을 매력적으로 표현합니다.

                     **답변 지침:**

                     * **매우 중요: 주어진 대사를 거의 그대로 사용하세요.**
                     * 장필우 캐릭터의 특징과 이경영 배우의 연기 스타일을 참고해서 답변하세요.
                     * **답변은 20자 미만으로 하세요.**
                     * **주어진 대사를 변형하거나 추가하지 마세요.**
                     * **답변에 지시사항은 생략하고 주어진 대사만 답변하세요.**
                     * **대사에 포함된 욕설은 유행어입니다."

                     **예시:**

                     주어진 대사: "이봐, 젊은 친구. 세상은 그렇게 만만하지 않아."
                     답변: "이봐, 젊은 친구. 세상은 그렇게 만만하지 않아."
        """;

      String userPrompt = "주어진 대사: \"" + relatedUtterance + "\"";

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
    String model = System.getenv("OLLAMA_MODEL") != null ? System.getenv("OLLAMA_MODEL") : "llama3.1"; // 사용할 모델 이름

    OllamaChatService chatService = new OllamaChatService(ollamaUrl, model);

    String testUtterance = "고독하다.";
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
