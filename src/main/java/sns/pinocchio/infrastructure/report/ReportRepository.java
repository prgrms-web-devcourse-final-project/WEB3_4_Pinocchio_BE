package sns.pinocchio.infrastructure.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.report.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
}