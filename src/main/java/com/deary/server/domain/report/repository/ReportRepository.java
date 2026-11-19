package com.deary.server.domain.report.repository;

import com.deary.server.domain.report.entity.Report;
import com.deary.server.domain.report.entity.ReportSource;
import com.deary.server.domain.report.entity.ReportStatus;
import com.deary.server.domain.report.entity.ReportTargetType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

	Optional<Report> findReportByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType,
			Long targetId);

	Optional<Report> findReportBySourceAndTargetTypeAndTargetId(ReportSource source, ReportTargetType targetType,
			Long targetId);

	List<Report> findAllReportByStatusOrderByIdDesc(ReportStatus status, Limit limit);

	List<Report> findAllReportByTargetTypeAndTargetIdOrderByIdAsc(ReportTargetType targetType, Long targetId);

	long countReportByTargetUserId(Long targetUserId);

	boolean existsReportByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType,
			Long targetId);

}
