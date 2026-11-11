package com.deary.server.domain.correction.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record CorrectionFindResponse(
		Long correctionId,
		Long diaryId,
		Corrector corrector,
		String comment,
		Instant correctedAt,
		Instant readAt,
		List<Item> items
) {

	public record Corrector(
			Long userId,
			String nickname,
			String profileImageUrl
	) {
	}

	@Schema(name = "CorrectionResultItem")
	public record Item(
			Long sentenceId,
			int sentenceIndex,
			String original,
			boolean changed,
			String correctedContent,
			String reason
	) {
	}

}
