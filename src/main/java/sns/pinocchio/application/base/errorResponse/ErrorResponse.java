package sns.pinocchio.application.base.errorResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 에러 응답 DTO
@Getter
@AllArgsConstructor
public class ErrorResponse {
  private String status; // 상태 (예: "error")
  private int statusCode; // 상태 코드 (예: 400)
  private String message; // 메시지 (예: "이미 사용 중인 이메일입니다.")
  private String code; // 오류 코드 (예: "USER_404")
}
