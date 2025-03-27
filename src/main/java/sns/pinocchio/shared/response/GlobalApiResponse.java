package sns.pinocchio.shared.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class GlobalApiResponse<T> {

  private String status;
  private int statusCode;
  private String message;
  private T data;

  public static <T> GlobalApiResponse<T> success(String message, T data) {
    return new GlobalApiResponse<>("success", 200, message, data);
  }

  public static <T> GlobalApiResponse<List<T>> error(int statusCode, String message) {
    return new GlobalApiResponse<>("error", statusCode, message, Collections.emptyList());
  }
}
