package sns.pinocchio.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreateEmbeddings {
  public static void main(String[] args){
    String uri = System.getenv("ATLAS_CONNECTION_STRING");
    if (uri == null || uri.isEmpty()) {
      throw new RuntimeException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
    }

    try {
      // Read and preprocess JSON data
      List<String> processedTexts = preprocessData("src/main/resources/data1.json");

      // Establish MongoDB connection
      try (MongoClient mongoClient = MongoClients.create(uri)) {
        MongoDatabase database = mongoClient.getDatabase("rag_db");
        MongoCollection<Document> collection = database.getCollection("scenes");

        System.out.println("Creating embeddings for " + processedTexts.size() + " documents");

        EmbeddingProvider embeddingProvider = new EmbeddingProvider();

        // Generate embeddings for processed data
        List<BsonArray> embeddings = embeddingProvider.getEmbeddings(processedTexts);

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < processedTexts.size(); i++) {
          Document doc = new Document("text", processedTexts.get(i))
                  .append("embedding", embeddings.get(i));
          documents.add(doc);
        }

        // Insert the embeddings into the Atlas collection
        List<String> insertedIds = new ArrayList<>();
        try {
          InsertManyResult result = collection.insertMany(documents);
          result.getInsertedIds().values()
                  .forEach(doc -> insertedIds.add(doc.toString()));

          System.out.println("Inserted " + insertedIds.size() + " documents with the following ids to " +
                  collection.getNamespace() + " collection: \n " + insertedIds);
        } catch (MongoException me) {
          throw new RuntimeException("Failed to insert documents", me);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Operation failed: ", e);
    }
  }

  /**
   * Preprocess data from JSON file
   * @param filePath Path to the JSON file
   * @return List of processed text strings
   */
  private static List<String> preprocessData(String filePath) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(new File(filePath));

    List<String> processedTexts = new ArrayList<>();

    // Extract character descriptions
    JsonNode charactersNode = rootNode.get("character");
    if (charactersNode != null && charactersNode.isArray()) {
      processedTexts.addAll(
              charactersNode.findValues("description").stream()
                      .map(JsonNode::asText)
                      .map(CreateEmbeddings::preprocessText)
                      .collect(Collectors.toList())
      );
    }

    // Extract scene dialogues
    JsonNode scenesNode = rootNode.get("scenes");
    if (scenesNode != null && scenesNode.isArray()) {
      processedTexts.addAll(
              scenesNode.findValues("dialogues").stream()
                      .flatMap(dialoguesNode ->
                              dialoguesNode.findValues("line").stream()
                                      .map(JsonNode::asText)
                                      .map(CreateEmbeddings::preprocessText)
                      )
                      .collect(Collectors.toList())
      );
    }

    return processedTexts;
  }

  /**
   * Preprocess text by removing unnecessary whitespace and newlines
   * @param text Input text
   * @return Processed text
   */
  private static String preprocessText(String text) {
    // Remove extra whitespaces, normalize newlines
    return text.trim()
            .replaceAll("\\s+", " ")
            .replaceAll("\n", " ");
  }
}
