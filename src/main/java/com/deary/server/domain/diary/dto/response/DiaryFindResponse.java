package com.deary.server.domain.diary.dto.response;

import com.deary.server.domain.correction.entity.CorrectionBadge;
import com.deary.server.domain.correction.entity.CorrectionProgress;
import com.deary.server.domain.profile.entity.Language;
import java.time.LocalDate;
import java.util.List;

public record DiaryFindResponse(
		Long diaryId,
		LocalDate diaryDate,
		String title,
		String content,
		Language language,
		List<DiaryPhotoResponse> photos,
		CorrectionProgress correctionProgress,
		CorrectionBadge correctionBadge
) {
}
