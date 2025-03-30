package sns.pinocchio.application.report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sns.pinocchio.domain.report.Report;
import sns.pinocchio.domain.report.ReportedType;
import sns.pinocchio.infrastructure.report.ReportRepository;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    // 신고 내역 저장
    public void createReport(Long reporterId, Long reportedId, ReportedType reportedType, String reason) {
        Report report = Report.builder()
                .reporterId(reporterId)
                .reportedId(reportedId)
                .reportedType(reportedType)
                .reason(reason)
                .build();

        reportRepository.save(report);
    }
}