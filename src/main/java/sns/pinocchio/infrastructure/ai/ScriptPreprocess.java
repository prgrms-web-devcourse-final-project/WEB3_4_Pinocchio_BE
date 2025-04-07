package sns.pinocchio.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.KoreanPosJava;
import scala.collection.Seq;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ScriptPreprocess {
  private final Set<String> stopwords;
  private final ObjectMapper objectMapper;

  public ScriptPreprocess(String stopwordsPath) throws IOException {
    this.stopwords = loadStopwords(stopwordsPath);
    this.objectMapper = new ObjectMapper();
    this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON pretty print
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
  public List<Map<String, Object>> loadAndExtract(String resourcePath, String textFieldName) throws IOException {
    List<Map<String, Object>> dataList = new ArrayList<>();
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

    if (inputStream == null) {
      throw new IOException("resources 폴더 안에 데이터 파일이 없습니다. 경로: " + resourcePath);
    }

    JsonNode rootNode = objectMapper.readTree(inputStream);

    if (rootNode.isArray()) {
      for (JsonNode node : rootNode) {
        Map<String, Object> data = objectMapper.convertValue(node, Map.class);
        dataList.add(data);
      }
    } else if (rootNode.isObject()) {
      JsonNode scenesNode = rootNode.get("scenes");

      if (scenesNode != null && scenesNode.isArray()) {
        for (JsonNode sceneNode : scenesNode) {
          Map<String, Object> data = objectMapper.convertValue(sceneNode, Map.class);
          dataList.add(data);
        }
      } else {
        System.out.println("Warning: 'scenes' array not found or not an array in the root object. Trying to extract from root object itself.");
        Map<String, Object> data = objectMapper.convertValue(rootNode, Map.class);
        dataList.add(data);
      }
    } else {
      System.err.println("Warning: JSON root is neither an array nor an object in " + resourcePath);
    }
    return dataList;
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
            KoreanPosJava.Noun.toString(), KoreanPosJava.Verb.toString(), KoreanPosJava.Adjective.toString(), KoreanPosJava.Adverb.toString(),
            KoreanPosJava.Exclamation.toString(), KoreanPosJava.Modifier.toString());

    return tokenList.stream()
            .filter(token -> allowedPos.contains(token.getPos().toString()))
            .map(KoreanTokenJava::getText)
            .filter(tokenText -> !stopwords.contains(tokenText))
            .collect(Collectors.toList());
  }

  public void savePreprocessedData(List<Map<String, Object>> dataList, String outputFilePath) throws IOException {
    List<Map<String, Object>> preprocessedList = new ArrayList<>();
    for (Map<String, Object> originalData : dataList) {
      if (originalData.containsKey("utterance")) {
        String originalText = (String) originalData.get("utterance");
        List<String> processedTokens = preprocessText(originalText);
        Map<String, Object> preprocessedData = new HashMap<>(originalData); // 기존 메타데이터 복사
        preprocessedData.put("processed_tokens", processedTokens); // processed_tokens 추가
        preprocessedList.add(preprocessedData);
      }
    }
    objectMapper.writeValue(new File(outputFilePath), preprocessedList);
    System.out.println("전처리된 데이터가 " + outputFilePath + "에 저장되었습니다.");
  }

  public static void main(String[] args) {
    String stopwordsPath = "stopwords.txt"; // resources 폴더에 있어야 함
    String inputFilePath = "data1.json"; // resources 폴더에 있어야 함
    String outputFilePath = "src/main/resources/preprocessed_data.json"; // 저장될 파일 경로

    try {
      ScriptPreprocess preprocess = new ScriptPreprocess(stopwordsPath);
      List<Map<String, Object>> rawData = preprocess.loadAndExtract(inputFilePath, "utterance");
      preprocess.savePreprocessedData(rawData, outputFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
