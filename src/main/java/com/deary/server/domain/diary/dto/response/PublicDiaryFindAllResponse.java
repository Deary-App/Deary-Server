package com.deary.server.domain.diary.dto.response;

import com.deary.server.domain.correction.entity.CorrectionProgress;
import com.deary.server.domain.profile.entity.Language;
import java.time.LocalDate;
import java.util.List;

public record PublicDiaryFindAllResponse(
		List<PublicDiaryCard> diaries,
		Long nextCursor
) {

	public record PublicDiaryCard(
			Long diaryId,
			LocalDate diaryDate,
			String title,
			Language language,
			String firstPhotoUrl,
			DiaryAuthorResponse author,
			CorrectionProgress correctionProgress
	) {
	}

}
