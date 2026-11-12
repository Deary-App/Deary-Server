package com.deary.server.domain.report.event;

import com.deary.server.domain.report.entity.ReportReason;
import com.deary.server.domain.report.entity.ReportTargetType;
import java.time.Instant;

public record ReportRegisteredEvent(
		Long reportId,
		ReportTargetType targetType,
		Long targetId,
		ReportReason reason,
		Instant createdAt
) {
}
