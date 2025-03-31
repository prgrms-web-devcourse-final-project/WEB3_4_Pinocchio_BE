package sns.pinocchio.infrastructure.shared.swagger;

public class ErrorExamples {

  public static final String SEND_CHAT_BAD_REQUEST_EXAMPLE =
      """
            {
              "status": "error",
              "statusCode": 400,
              "message": "입력값이 유효하지 않습니다.",
              "data": []
            }
          """;

  public static final String SEND_CHAT_INTERNAL_SERVER_ERROR_EXAMPLE =
      """
            {
              "status": "error",
              "statusCode": 500,
              "message": "메시지 전송에 실패했습니다. 다시 시도해주세요.",
              "data": []
            }
          """;

  public static final String SEND_CHAT_NOTIFICATION_INTERNAL_SERVER_ERROR_EXAMPLE =
      """
            {
              "status": "error",
              "statusCode": 500,
              "message": "메시지 알림 전송에 실패했습니다.",
              "data": []
            }
          """;

  public static final String FIND_CHAT_ROOM_NOT_FOUND_EXAMPLE =
      """
      {
        "status": "error",
        "statusCode": 404,
        "message": "등록된 사용자를 찾을 수 없습니다.",
        "data": []
      }
    """;
}
