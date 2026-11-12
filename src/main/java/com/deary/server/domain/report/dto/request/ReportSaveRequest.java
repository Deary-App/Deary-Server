package com.deary.server.domain.report.dto.request;

import com.deary.server.domain.report.entity.ReportReason;
import com.deary.server.domain.report.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ReportSaveRequest(
		@NotNull(message = "신고 대상 종류를 선택해 주세요.")
		ReportTargetType targetType,

		@NotNull(message = "신고 대상 ID를 입력해 주세요.")
		@Positive(message = "신고 대상 ID가 올바르지 않습니다.")
		Long targetId,

		@NotNull(message = "신고 사유를 선택해 주세요.")
		ReportReason reason,

		@Size(max = 500, message = "신고 내용은 500자 이하로 입력해 주세요.")
		String detail
) {
}
