package sns.pinocchio.domain.report;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static sns.pinocchio.domain.report.ReportStatus.PENDING;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Report {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long reportId;

  private Long reporterId;

  @Enumerated(EnumType.STRING)
  private ReportedType reportedType;

  private Long reportedId;

  private String reason;

  @Enumerated(EnumType.STRING)
  private ReportStatus status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  @Builder
  public Report(Long reporterId, ReportedType reportedType, Long reportedId, String reason) {
    this.reporterId = reporterId;
    this.reportedType = reportedType;
    this.reportedId = reportedId;
    this.reason = reason;
    this.status = PENDING;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }
}
