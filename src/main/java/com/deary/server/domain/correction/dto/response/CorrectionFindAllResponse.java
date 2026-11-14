package com.deary.server.domain.correction.dto.response;

import com.deary.server.domain.correction.entity.CorrectionReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
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
			Instant correctedAt,
			@Schema(description = "이 교정에 받은 반응 종류 (예: THANKS)")
			List<CorrectionReactionType> reactions,
			@Schema(description = "아직 확인하지 않은 반응이 있는지")
			boolean hasUnreadReaction
	) {
	}

}
