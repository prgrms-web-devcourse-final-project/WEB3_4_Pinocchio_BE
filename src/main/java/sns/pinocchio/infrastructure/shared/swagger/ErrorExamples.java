package sns.pinocchio.infrastructure.shared.swagger;

public class ErrorExamples {

  public static final String NOTIFICATION_UPDATE_BAD_REQUEST_EXAMPLE =
      """
        {
          "status": "error",
          "statusCode": 400,
          "message": "입력값이 유효하지 않습니다.",
          "data": []
        }
      """;

  public static final String NOTIFICATION_FIND_BAD_REQUEST_EXAMPLE =
      """
        {
          "status": "error",
          "statusCode": 400,
          "message": "[userId] 정보가 존재하지 않습니다.",
          "data": []
        }
      """;

  public static final String NOTIFICATION_INTERNAL_SERVER_ERROR_EXAMPLE =
      """
        {
          "status": "error",
          "statusCode": 500,
          "message": "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
          "data": []
        }
      """;
}
