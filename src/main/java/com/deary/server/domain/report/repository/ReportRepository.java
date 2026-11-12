package com.deary.server.domain.report.repository;

import com.deary.server.domain.report.entity.Report;
import com.deary.server.domain.report.entity.ReportTargetType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

	Optional<Report> findReportByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType,
			Long targetId);

	boolean existsReportByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType,
			Long targetId);

}
