package com.deary.server.domain.report.dto.response;

import com.deary.server.domain.report.entity.ReportReason;
import com.deary.server.domain.report.entity.ReportSource;
import com.deary.server.domain.report.entity.ReportStatus;
import com.deary.server.domain.report.entity.ReportTargetType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 운영자 신고 상세. 대상이 삭제됐으면 diary·correction이 비어 있다. 신고한 사람은 내려주지 않는다.
 */
public record ReportFindResponse(
		Long reportId,
		ReportTargetType targetType,
		Long targetId,
		ReportStatus status,
		String memo,
		Instant handledAt,
		List<Entry> reports,
		TargetUser targetUser,
		DiaryTarget diary,
		CorrectionTarget correction
) {

	public record Entry(
			Long reportId,
			ReportSource source,
			ReportReason reason,
			String detail,
			Instant createdAt
	) {
	}

	public record TargetUser(
			Long userId,
			String nickname,
			String profileImageUrl,
			boolean profileImageHidden,
			boolean suspended,
			long reportCount
	) {
	}

	public record DiaryTarget(
			Long diaryId,
			LocalDate diaryDate,
			String title,
			String content,
			List<String> photoUrls,
			boolean hidden
	) {
	}

	public record CorrectionTarget(
			Long correctionId,
			Long diaryId,
			String comment,
			List<Item> items,
			boolean hidden
	) {
	}

	public record Item(
			String originalContent,
			String correctedContent,
			String reason
	) {
	}

}
