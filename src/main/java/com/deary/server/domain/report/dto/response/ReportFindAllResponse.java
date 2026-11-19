package com.deary.server.domain.report.dto.response;

import com.deary.server.domain.report.entity.ReportReason;
import com.deary.server.domain.report.entity.ReportStatus;
import com.deary.server.domain.report.entity.ReportTargetType;
import java.time.Instant;
import java.util.List;

/**
 * 운영자 신고함. 같은 대상에 들어온 신고는 한 줄로 묶는다.
 */
public record ReportFindAllResponse(
		List<ReportGroup> reports
) {

	public record ReportGroup(
			Long reportId,
			ReportTargetType targetType,
			Long targetId,
			List<ReportReason> reasons,
			int reportCount,
			Instant reportedAt,
			ReportStatus status
	) {
	}

}
