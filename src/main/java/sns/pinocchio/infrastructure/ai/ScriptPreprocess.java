package sns.pinocchio.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import scala.collection.Seq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ScriptPreprocess {
  private final Set<String> stopwords;
  private final ObjectMapper objectMapper;

  public ScriptPreprocess(String stopwordsPath) throws IOException {
    this.stopwords = loadStopwords(stopwordsPath);
    this.objectMapper = new ObjectMapper();
  }

  private Set<String> loadStopwords(String resourcePath) throws IOException {
    Set<String> stopwordsSet = new HashSet<>();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

    if (inputStream == null) {
      throw new IOException("resources 폴더 안에 불용어 사전이 없습니다. 경로:" + resourcePath);
    }

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stopwordsSet.add(line.trim());
      }
    }
    return stopwordsSet;
  }


  //JSON 파일에서 대화 데이터를 로드 후 utterance를 추출
  public List<String> loadAndExtractText(String resourcePath, String textFieldName) throws IOException {
    List<String> textList = new ArrayList<>();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

    if (inputStream == null) {
      throw new IOException("resources 폴더 안에 데이터 파일이 없습니다. 경로: " + resourcePath);
    }

    JsonNode rootNode = objectMapper.readTree(inputStream);

    if (rootNode.isArray()) {
      for (JsonNode node : rootNode) {
        if (node.has(textFieldName)) {
          textList.add(node.get(textFieldName).asText());
        } else {
          System.err.println("Warning: Array element missing field '" + textFieldName + "': " + node.toString());
        }
      }
    } else if (rootNode.isObject()) {
      JsonNode scenesNode = rootNode.get("scenes");

      if (scenesNode != null && scenesNode.isArray()) {
        for (JsonNode sceneNode : scenesNode) {
          if (sceneNode.has(textFieldName)) {
            textList.add(sceneNode.get(textFieldName).asText());
          } else {
            System.err.println("Warning: Scene object missing field '" + textFieldName + "': " + sceneNode.toString());
          }
        }
      } else {
        System.out.println("Warning: 'scenes' array not found or not an array in the root object. Trying to extract from root object itself.");
        if (rootNode.has(textFieldName)) {
          textList.add(rootNode.get(textFieldName).asText());
        } else {
          System.err.println(textFieldName + "이 없습니다.");
        }
      }
    } else {
      System.err.println("Warning: JSON root is neither an array nor an object in " + resourcePath);
    }
    return textList;
  }

  public List<String> preprocessText(String text) {
    // 1. 정규화
    CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

    // 2. 토크나이저
    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);
    List<KoreanTokenJava> tokenList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);
    System.out.println("Token List: " + tokenList);

    // 3. 불용어 제거
    Set<String> allowedPos = Set.of(
            "Noun", "Verb", "Adjective", "Adverb", "Exclamation", "Modifier");

    return tokenList.stream()
            .filter(token -> allowedPos.contains(token.getPos().toString()))
            .map(KoreanTokenJava::getText)
            .filter(tokenText -> !stopwords.contains(tokenText))
            .collect(Collectors.toList());
  }

  public static void main(String[] args) {
    try {

      String jsonFilePath = "data1.json";
      String stopwordsFilePath = "stopwords.txt";
      String fieldToExtract = "utterance";

      ScriptPreprocess preprocessor = new ScriptPreprocess(stopwordsFilePath);

      List<String> dialogues = preprocessor.loadAndExtractText(jsonFilePath, fieldToExtract);

      for (String dialogue : dialogues) {
        System.out.println("원본: " + dialogue);
        List<String> processedTokens = preprocessor.preprocessText(dialogue);
        System.out.println("전처리: " + processedTokens);        System.out.println("---");
      }

    } catch (IOException e) {
      System.err.println("전처리 에러: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
