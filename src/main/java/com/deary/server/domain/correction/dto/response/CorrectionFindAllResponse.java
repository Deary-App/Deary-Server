package com.deary.server.domain.correction.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CorrectionFindAllResponse(
		List<Activity> corrections,
		Long nextCursor
) {

	public record Activity(
			Long correctionId,
			Long diaryId,
			LocalDate diaryDate,
			String excerpt,
			String comment,
			Instant correctedAt
	) {
	}

}
