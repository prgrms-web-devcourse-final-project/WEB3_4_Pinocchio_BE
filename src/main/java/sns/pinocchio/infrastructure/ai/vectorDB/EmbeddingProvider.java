package sns.pinocchio.infrastructure.ai.vectorDB;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.bson.BsonArray;
import org.bson.BsonDouble;

import java.util.List;

import static java.time.Duration.ofSeconds;

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

  public BsonArray getEmbedding(String text) {
    Response<Embedding> response = createEmbeddingModel().embed(text);

    return new BsonArray(
            response.content().vectorAsList().stream()
                    .map(BsonDouble::new)
                    .toList());
  }
}
