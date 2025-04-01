package sns.pinocchio.infrastructure.ai;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.search.FieldSearchPath;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import sns.pinocchio.infrastructure.ai.EmbeddingProvider;

import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.vectorSearch;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Projections.metaVectorSearchScore;
import static com.mongodb.client.model.search.SearchPath.fieldPath;
import static com.mongodb.client.model.search.VectorSearchOptions.exactVectorSearchOptions;
import static java.util.Arrays.asList;
public class VectorQuery {
    /**
     * 주어진 쿼리 문자열에 대해 벡터 유사성 검색을 수행합니다.
     *
     * @param queryString 검색할 쿼리 문자열
     * @param limit 반환할 최대 결과 수
     * @return 유사한 문서 목록
     */
    public static List<SimilarityResult> searchSimilarDocuments(String queryString, int limit) {
      String uri = System.getenv("ATLAS_CONNECTION_STRING");
      if (uri == null || uri.isEmpty()) {
        throw new IllegalStateException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
      }

      List<SimilarityResult> similarityResults = new ArrayList<>();

      try (MongoClient mongoClient = MongoClients.create(uri)) {
        MongoDatabase database = mongoClient.getDatabase("rag_db");
        MongoCollection<Document> collection = database.getCollection("scenes");

        // 쿼리 문자열에 대한 임베딩 생성
        EmbeddingProvider embeddingProvider = new EmbeddingProvider();
        BsonArray embeddingBsonArray = embeddingProvider.getEmbedding(queryString);
        List<Double> embedding = new ArrayList<>();
        for (BsonValue value : embeddingBsonArray.stream().toList()) {
          embedding.add(value.asDouble().getValue());
        }

        // 벡터 검색 파이프라인 설정
        String indexName = "vector_index";
        FieldSearchPath fieldSearchPath = fieldPath("scenes");

        List<Bson> pipeline = asList(
                vectorSearch(
                        fieldSearchPath,
                        embedding,
                        indexName,
                        limit,
                        exactVectorSearchOptions()
                ),
                project(
                        fields(exclude("_id"), include("text"),
                                metaVectorSearchScore("score"))
                )
        );

        // 쿼리 실행 및 결과 처리
        List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

        for (Document doc : results) {
          similarityResults.add(new SimilarityResult(
                  doc.getString("text"),
                  doc.getDouble("score")
          ));
        }

        return similarityResults;
      } catch (MongoException me) {
        throw new RuntimeException("Failed to connect to MongoDB", me);
      } catch (Exception e) {
        throw new RuntimeException("Operation failed", e);
      }
    }

    /**
     * 유사성 검색 결과를 저장하는 정적 내부 클래스
     */
    public static class SimilarityResult {
      private String text;
      private double score;

      public SimilarityResult(String text, double score) {
        this.text = text;
        this.score = score;
      }

      public String getText() {
        return text;
      }

      public double getScore() {
        return score;
      }

      @Override
      public String toString() {
        return "SimilarityResult{" +
                "text='" + text + '\'' +
                ", score=" + score +
                '}';
      }
    }

    // 사용 예시 메서드
    public static void main(String[] args) {
      String queryString = "정의를 실현하기 위해서 졸라 고독할 수밖에 없거든";
      int limit = 2;

      List<SimilarityResult> results = searchSimilarDocuments(queryString, limit);

      if (results.isEmpty()) {
        System.out.println("No similar documents found.");
      } else {
        System.out.println("Similar documents:");
        for (SimilarityResult result : results) {
          System.out.println("Text: " + result.getText());
          System.out.println("Similarity Score: " + result.getScore());
          System.out.println("---");
        }
      }
    }
}