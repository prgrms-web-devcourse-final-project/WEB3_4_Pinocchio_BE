package sns.pinocchio.infrastructure.ai.vectorDB;

import static java.time.Duration.*;

import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonDouble;
import org.springframework.beans.factory.annotation.Value;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.output.Response;

public class EmbeddingProvider {
  private static HuggingFaceEmbeddingModel embeddingModel= createEmbeddingModel();

  private static HuggingFaceEmbeddingModel createEmbeddingModel() {
    if (embeddingModel == null) {
      String accessToken = System.getenv("HUGGING_FACE_ACCESS_TOKEN");
      if (accessToken == null || accessToken.isEmpty()) {
        throw new RuntimeException("HUGGING_FACE_ACCESS_TOKEN env variable is not set or is empty.");
      }
      return HuggingFaceEmbeddingModel.builder()
              .accessToken(accessToken)
              .modelId("intfloat/multilingual-e5-large-instruct")
              .waitForModel(true)
              .timeout(ofSeconds(60))
              .build();
    }
    return embeddingModel;
  }


  public List<BsonArray> getEmbeddings(List<String> texts) {
    List<TextSegment> textSegments = texts.stream()
            .map(TextSegment::from)
            .toList();

    Response<List<Embedding>> response = createEmbeddingModel().embedAll(textSegments);

    return response.content().stream()
            .map(e -> new BsonArray(
                    e.vectorAsList().stream()
                            .map(BsonDouble::new)
                            .toList()))
            .toList();
  }

  public BsonArray getEmbedding(String text, int retryCount) throws InterruptedException {
    Response<Embedding> response;
    try {
      response = createEmbeddingModel().embed(text);
    } catch (RuntimeException e){
      if (retryCount >= 10) {
        return new BsonArray();
      }
      Thread.sleep( 10000L);
      return getEmbedding(text, retryCount+1);
    }
    return new BsonArray(
            response.content().vectorAsList().stream()
                    .map(BsonDouble::new)
                    .toList());
  }
}
