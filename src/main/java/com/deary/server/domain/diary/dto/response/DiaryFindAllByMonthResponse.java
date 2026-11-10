package com.deary.server.domain.diary.dto.response;

import com.deary.server.domain.correction.entity.CorrectionProgress;
import java.time.LocalDate;
import java.util.List;

public record DiaryFindAllByMonthResponse(
		int year,
		int month,
		List<DiarySummary> diaries
) {

	public record DiarySummary(
			Long diaryId,
			LocalDate diaryDate,
			String title,
			String firstPhotoUrl,
			CorrectionProgress correctionProgress
	) {
	}

}
