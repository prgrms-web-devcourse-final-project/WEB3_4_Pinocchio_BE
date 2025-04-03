package sns.pinocchio.infrastructure.ai;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collections;
import java.util.List;

  public class CreateIndex {
    public static void main(String[] args) {
      String uri = System.getenv("ATLAS_CONNECTION_STRING");
      if (uri == null || uri.isEmpty()) {
        throw new IllegalStateException("ATLAS_CONNECTION_STRING env variable is not set or is empty.");
      }

      // Dimensions for paraphrase-multilingual-MiniLM-L12-v2 model
      int EMBEDDING_DIMENSIONS = 384;

      // establish connection and set namespace
      try (MongoClient mongoClient = MongoClients.create(uri)) {
        MongoDatabase database = mongoClient.getDatabase("rag_db");
        MongoCollection<Document> collection = database.getCollection("scenes");

        // define the index details
        String indexName = "korean_multilingual_vector_index";

        Bson definition = new Document(
                "fields",
                Collections.singletonList(
                        new Document("type", "vector")
                                .append("path", "embedding")
                                .append("numDimensions", EMBEDDING_DIMENSIONS)
                                .append("similarity", "cosine")));

        // define the index model using the specified details
        SearchIndexModel indexModel = new SearchIndexModel(
                indexName,
                definition,
                SearchIndexType.vectorSearch());

        // Create the index using the model
        try {
          List<String> result = collection.createSearchIndexes(Collections.singletonList(indexModel));
          System.out.println("Successfully created a vector index named: " + result);
        } catch (Exception e) {
          throw new RuntimeException("Failed to create search index", e);
        }

        // Wait for Atlas to build the index and make it queryable
        System.out.println("Polling to confirm the index has completed building.");
        System.out.println("It may take up to a minute for the index to build before you can query using it.");

        ListSearchIndexesIterable<Document> searchIndexes = collection.listSearchIndexes();
        Document doc = null;

        while (doc == null) {
          try (MongoCursor<Document> cursor = searchIndexes.iterator()) {
            if (!cursor.hasNext()) {
              break;
            }
            Document current = cursor.next();
            String name = current.getString("name");
            boolean queryable = current.getBoolean("queryable");

            if (name.equals(indexName) && queryable) {
              doc = current;
            } else {
              Thread.sleep(500);
            }
          } catch (Exception e) {
            throw new RuntimeException("Error checking index status", e);
          }
        }

        System.out.println(indexName + " index is ready to query");
      } catch (MongoException me) {
        throw new RuntimeException("Failed to connect to MongoDB ", me);
      } catch (Exception e) {
        throw new RuntimeException("Operation failed: ", e);
      }
    }
}
