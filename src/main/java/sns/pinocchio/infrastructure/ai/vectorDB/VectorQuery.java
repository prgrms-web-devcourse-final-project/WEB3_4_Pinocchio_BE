package sns.pinocchio.infrastructure.ai.vectorDB;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.search.VectorSearchOptions;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import scala.collection.Seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Aggregates.project;
import static com.mongodb.client.model.Aggregates.vectorSearch;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.search.SearchPath.fieldPath;

public class VectorQuery {

  private static final Set<String> stopwords;

  private static final Set<String> allowedPos = Set.of(
          "Noun", "Verb", "Adjective", "Adverb", "Exclamation", "KoreanParticle"
  );

  private static final String STOPWORDS_RESOURCE_PATH = "stopwords.txt";

  static {
    try {
      stopwords = loadStopwords(STOPWORDS_RESOURCE_PATH);
    } catch (IOException e) {
      System.err.println("Failed to load stopwords from " + STOPWORDS_RESOURCE_PATH + ": " + e.getMessage());
      throw new RuntimeException("Failed to initialize VectorQuery due to stopword loading failure.", e);
    }
  }

  private static Set<String> loadStopwords(String resourcePath) throws IOException {
    Set<String> stopwordsSet = new HashSet<>();
    InputStream inputStream = VectorQuery.class.getClassLoader().getResourceAsStream(resourcePath);

    if (inputStream == null) {
      throw new IOException("불용어 파일이 해당 경로에 없습니다: " + resourcePath);
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stopwordsSet.add(line.trim());
      }
    }
    return stopwordsSet;
  }
  private static String preprocessQueryText(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }
    // 1. 정규화 (Normalize)
    CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

    // 2. 토큰화 (Tokenize)
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
    List<KoreanTokenJava> tokenList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

    // 3. 품사 필터링 및 불용어 제거, 공백으로 연결
    String processedText = tokenList.stream()
            .filter(token -> allowedPos.contains(token.getPos().toString()))
            .map(KoreanTokenJava::getText)
            .filter(tokenText -> !stopwords.contains(tokenText))
            .collect(Collectors.joining(" "));

    System.out.println("입력된 쿼리문: " + text);
    System.out.println("전처리된 쿼리문: " + processedText);
    return processedText;
  }

  public static List<SimilarityResult> searchSimilarDocuments(String queryString, int limit) {
    String uri = System.getenv("ATLAS_CONNECTION_STRING");
    if (uri == null || uri.isEmpty()) {
      throw new IllegalStateException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
    }

    List<SimilarityResult> similarityResults = new ArrayList<>();

    try (MongoClient mongoClient = MongoClients.create(uri)) {
      MongoDatabase database = mongoClient.getDatabase("rag_db");
      MongoCollection<Document> collection = database.getCollection("scenes");

      // 1. 쿼리 문자열 전처리
      String processedQuery = preprocessQueryText(queryString);

      // 전처리 결과가 비어있으면 검색 의미 없음
      if (processedQuery.isEmpty()) {
        System.out.println("전처리 결과가 비어있습니다.");
        return similarityResults;
      }

      // 2. 전처리된 쿼리 문자열에 대한 임베딩 생성
      EmbeddingProvider embeddingProvider = new EmbeddingProvider();
      BsonArray embeddingBsonArray = embeddingProvider.getEmbedding(processedQuery,0);

      List<Double> queryEmbedding = embeddingBsonArray.stream()
              .map(bsonValue -> bsonValue.asDouble().getValue())
              .collect(Collectors.toList());

      System.out.println(embeddingBsonArray);
      // 3. 벡터 검색 파이프라인 설정
      String indexName = "korean_multilingual_vector_index";
      String embeddingFieldPath = "embedding";

      List<Bson> pipeline = List.of(
              vectorSearch(
                      fieldPath(embeddingFieldPath),
                      queryEmbedding,
                      indexName,
                      limit,
                      VectorSearchOptions.exactVectorSearchOptions()
//                       .filter(Filters.eq("emotion", "some_value")) // 필요시 메타데이터 필터 추가
              ),
              project( // 결과 필드 프로젝션 수정
                      fields(
                              excludeId(),
                              include("utterance"),
                              include("weight"),
                              metaVectorSearchScore("score")
                      )
              )
      );

      // 쿼리 실행 및 결과 처리
      List<Document> results = collection.aggregate(pipeline).into(new ArrayList<>());

      for (Document doc : results) {
        double baseScore = doc.getDouble("score");
        int weight = doc.getInteger("weight", 1);
        double adjustedScore = baseScore * weight;

        similarityResults.add(new SimilarityResult(
                doc.getString("utterance"),
                adjustedScore,
                doc.getList("embedding", Double.class)
        ));
      }

      return similarityResults;

    } catch (MongoException me) {
      System.err.println("MongoDB connection or query failed: " + me.getMessage());
      throw new RuntimeException("Failed to connect or query MongoDB", me);
    } catch (Exception e) {
      System.err.println("An unexpected error occurred during vector search: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Vector search operation failed", e);
    }
  }

  public static class SimilarityResult {
    private final String utterance;
    private final double score;
    private final List<Double> embedding;

    public SimilarityResult(String utterance, double score, List<Double> embedding) {
      this.utterance = utterance;
      this.score = score;
      this.embedding = embedding;
    }

    public String getUtterance() {
      return utterance;
    }

    public double getScore() {
      return score;
    }
    public List<Double> getEmbedding() {
      return embedding;
    }

    @Override
    public String toString() {
      return "SimilarityResult{" +
              "utterance='" + utterance + '\'' +
              ", score=" + score +
              ", embedding=" + embedding +
              '}';
    }
  }

  // 사용 예시 메서드
  public static void main(String[] args) {
    // 예시 쿼리 문자열
    String queryString = "그걸 왜 말을 못해? 병신이야?!";
    int limit = 10;

    try {
      List<SimilarityResult> results = searchSimilarDocuments(queryString, limit);

      if (results.isEmpty()) {
        System.out.println("\n문서를 찾지 못했습니다..");
      } else {
        System.out.println("\n찾은 문서들:");
        for (SimilarityResult result : results) {
          System.out.println("Utterance: " + result.getUtterance());
          System.out.println("Adjusted Score: " + result.getScore());
//          System.out.println("Embedding: " + result.getEmbedding());
          System.out.println("---");
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }
}