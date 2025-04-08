package sns.pinocchio.infrastructure.ai.vectorDB;

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
import org.bson.json.JsonParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateEmbeddings {

  public static void main(String[] args) {
    String uri = System.getenv("ATLAS_CONNECTION_STRING");
    if (uri == null || uri.isEmpty()) {
      throw new RuntimeException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
    }

    String filePath = "src/main/resources/preprocessed_data.json"; // 처리할 파일 경로

    try {
      // Read and load preprocessed data from JSON file
      // 각 항목은 Map<String, Object> 형태이며, 원본 데이터 필드와 처리된 토큰을 포함
      List<Map<String, Object>> preprocessedEntries = loadPreprocessedData(filePath);

      // 임베딩할 텍스트만 추출 (processed_tokens를 공백으로 연결)
      List<String> textsToEmbed = preprocessedEntries.stream()
              .map(entry -> (List<String>) entry.getOrDefault("processed_tokens", List.of())) // processed_tokens 가져오기
              .map(tokens -> String.join(" ", tokens)) // 토큰들을 공백으로 연결
              .collect(Collectors.toList());

      // Establish MongoDB connection
      try (MongoClient mongoClient = MongoClients.create(uri)) {
        MongoDatabase database = mongoClient.getDatabase("rag_db"); // DB 이름 확인
        MongoCollection<Document> collection = database.getCollection("scenes"); // Collection 이름 확인

        System.out.println("Loaded " + preprocessedEntries.size() + " entries from " + filePath);
        System.out.println("Creating embeddings for " + textsToEmbed.size() + " processed texts");

        // EmbeddingProvider 인스턴스 생성 (ai 패키지에 있다고 가정)
        EmbeddingProvider embeddingProvider = new EmbeddingProvider();

        // Generate embeddings for the joined processed tokens
        List<BsonArray> embeddings = embeddingProvider.getEmbeddings(textsToEmbed); // BsonArray 타입 확인 필요

        if (embeddings.size() != preprocessedEntries.size()) {
          throw new RuntimeException("Mismatch between number of preprocessed entries ("
                  + preprocessedEntries.size() + ") and generated embeddings (" + embeddings.size() + ")");
        }

        // Prepare documents for MongoDB insertion
        List<Document> documentsToInsert = new ArrayList<>();
        for (int i = 0; i < preprocessedEntries.size(); i++) {
          Map<String, Object> originalData = preprocessedEntries.get(i);
          BsonArray embedding = embeddings.get(i);

          Document doc = new Document()
                  // 원본 메타데이터 추가 (필요한 필드만 선택)
                  .append("dialogue_id", originalData.get("dialogue_id"))
                  .append("scene_number", originalData.get("scene_number"))
                  .append("speaker", originalData.get("speaker"))
                  .append("utterance", originalData.get("utterance"))
                  .append("context", originalData.get("context"))
                  .append("emotion", originalData.get("emotion"))
                  .append("keywords", originalData.get("keywords"))
                  .append("processed_tokens", originalData.get("processed_tokens"))
                  .append("embedding", embedding); // 필드 이름은 Atlas Vector Search 인덱스와 일치해야 함
          documentsToInsert.add(doc);
        }

        // Insert the documents into the Atlas collection
        List<String> insertedIds = new ArrayList<>();
        if (!documentsToInsert.isEmpty()) {
          try {
            InsertManyResult result = collection.insertMany(documentsToInsert);
            result.getInsertedIds().values()
                    .forEach(insertedId -> insertedIds.add(insertedId.asObjectId().getValue().toString())); // ObjectId에서 문자열 ID 추출

            System.out.println("Successfully inserted " + insertedIds.size() + " documents into " +
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

    // JSON 파일이 객체 배열이라고 가정하고 TypeReference를 사용하여 List<Map<String, Object>>로 읽음
    try {
      return objectMapper.readValue(inputFile, new TypeReference<List<Map<String, Object>>>() {});
    } catch (JsonParseException | com.fasterxml.jackson.databind.JsonMappingException e) {
      System.err.println("Failed to parse JSON file as a List of Maps. Ensure the file contains a valid JSON array.");
      throw e;
    }
  }
}
