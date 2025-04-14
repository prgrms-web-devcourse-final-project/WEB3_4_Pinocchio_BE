package sns.pinocchio.infrastructure.ai.vectorDB;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertManyResult;
import org.bson.BsonArray;
import org.bson.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateEmbeddingsPerToken {
  private static final String DB_NAME = "rag_db";
  private static final String COLLECTION_NAME = "scenes"; // 새로운 컬렉션 이름

  public static void main(String[] args) {
    String uri = System.getenv("ATLAS_CONNECTION_STRING");
    if (uri == null || uri.isEmpty()) {
      throw new RuntimeException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
    }

    String filePath = "src/main/resources/preprocessed_data.json"; // 처리할 파일 경로

    try {
      // Read and load preprocessed data from JSON file
      List<Map<String, Object>> preprocessedEntries = loadPreprocessedData(filePath);

      // EmbeddingProvider 인스턴스 생성 (ai 패키지에 있다고 가정)
      EmbeddingProvider embeddingProvider = new EmbeddingProvider();

      // Prepare documents for MongoDB insertion (each token will be a separate document)
      List<Document> documentsToInsert = new ArrayList<>();
      for (Map<String, Object> entry : preprocessedEntries) {
        List<String> tokens = (List<String>) entry.getOrDefault("processed_tokens", List.of());
        if (!tokens.isEmpty()) {
          List<BsonArray> embeddings = embeddingProvider.getEmbeddings(tokens); // 각 토큰에 대한 임베딩 생성

          if (embeddings.size() != tokens.size()) {
            System.err.println("Warning: Mismatch between number of tokens (" + tokens.size() +
                    ") and generated embeddings (" + embeddings.size() + ") for entry: " + entry.get("dialogue_id"));
            // 에러 처리 방식 결정 (skip, throw exception 등)
            continue;
          }

          for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            BsonArray embedding = embeddings.get(i);

            Document doc = new Document()
                    // 원본 메타데이터 추가 (필요한 필드만 선택)
                    .append("dialogue_id", entry.get("dialogue_id"))
                    .append("scene_number", entry.get("scene_number"))
                    .append("speaker", entry.get("speaker"))
                    .append("previous_utterance", entry.get("previous_utterance"))
                    .append("utterance", entry.get("utterance"))
                    .append("context", entry.get("context"))
                    .append("emotion", entry.get("emotion"))
                    .append("keywords", entry.get("keywords"))
                    .append("original_tokens", entry.get("processed_tokens")) // 원본 토큰 리스트 보존
                    .append("weight", entry.get("weight"))
                    .append("token", token) // 개별 토큰
                    .append("embedding", embedding); // 해당 토큰의 임베딩
            documentsToInsert.add(doc);
          }
        }
      }

      // Establish MongoDB connection
      try (MongoClient mongoClient = MongoClients.create(uri)) {
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);

        System.out.println("Loaded " + preprocessedEntries.size() + " entries from " + filePath);
        System.out.println("Creating embeddings and preparing " + documentsToInsert.size() + " documents for tokens");

        // Insert the documents into the Atlas collection
        List<String> insertedIds = new ArrayList<>();
        if (!documentsToInsert.isEmpty()) {
          try {
            InsertManyResult result = collection.insertMany(documentsToInsert);
            result.getInsertedIds().values()
                    .forEach(insertedId -> insertedIds.add(insertedId.asObjectId().getValue().toString())); // ObjectId에서 문자열 ID 추출

            System.out.println("Successfully inserted " + insertedIds.size() + " documents (one per token) into " +
                    collection.getNamespace());
            // System.out.println("Inserted document IDs: " + insertedIds); // 필요시 ID 출력
          } catch (MongoException me) {
            System.err.println("Failed to insert documents into MongoDB: " + me.getMessage());
            throw new RuntimeException("Failed to insert documents", me);
          }
        } else {
          System.out.println("No documents were prepared for insertion.");
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading or parsing the preprocessed data file: " + e.getMessage());
      throw new RuntimeException("Operation failed due to file error: ", e);
    } catch (Exception e) {
      System.err.println("An unexpected error occurred: " + e.getMessage());
      e.printStackTrace(); // 스택 트레이스 출력
      throw new RuntimeException("Operation failed: ", e);
    }
  }

  private static List<Map<String, Object>> loadPreprocessedData(String filePath) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    File inputFile = new File(filePath);

    if (!inputFile.exists()) {
      throw new IOException("File not found: " + filePath);
    }

    try {
      return objectMapper.readValue(inputFile, new TypeReference<List<Map<String, Object>>>() {});
    } catch (JsonParseException | com.fasterxml.jackson.databind.JsonMappingException e) {
      System.err.println("Failed to parse JSON file as a List of Maps. Ensure the file contains a valid JSON array.");
      throw e;
    }
  }
}
