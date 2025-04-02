package sns.pinocchio.presentation.report.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ReportErrorCode {
  REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "404-1", "신고 내역을 찾을 수 없습니다."),
  REPORT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "400-1", "이미 신고한 대상입니다."),
  INVALID_REPORT_REASON(HttpStatus.BAD_REQUEST, "400-2", "잘못된 신고 사유입니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}
