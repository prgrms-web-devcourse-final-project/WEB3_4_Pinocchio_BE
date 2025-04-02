package sns.pinocchio.infrastructure.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.pinocchio.domain.report.Report;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
  Optional<Report> findByReporterId(long id);
}
