package sns.pinocchio.presentation.report.exception;

import org.springframework.http.HttpStatus;

public class ReportException extends RuntimeException {
  private final ReportErrorCode reportErrorCode;

  public ReportException(ReportErrorCode reportErrorCode) {
    super(reportErrorCode.getMessage());
    this.reportErrorCode = reportErrorCode;
  }

  public ReportErrorCode getReportErrorCode() {
    return reportErrorCode;
  }

  public HttpStatus getStatus() {
    return reportErrorCode.getHttpStatus();
  }

  public String getCode() {
    return reportErrorCode.getCode();
  }
}
