package sns.pinocchio.infrastructure.shared.swagger;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Swagger 문서 내 에러 포맷 관련 클래스 (실제 구현과 관련 X) */
@Schema(description = "에러 응답 포맷")
public class ErrorResponseSchema {

  @Schema(description = "응답 상태", example = "error")
  private String status;

  @Schema(description = "HTTP 상태 코드", example = "400")
  private int statusCode;

  @Schema(description = "에러 메시지", example = "입력값이 유효하지 않습니다.")
  private String message;

  @Schema(description = "추가 데이터 (보통 빈 배열)", example = "[]")
  private List<Object> data;
}
