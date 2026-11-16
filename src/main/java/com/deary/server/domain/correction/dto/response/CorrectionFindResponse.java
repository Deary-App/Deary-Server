package com.deary.server.domain.correction.dto.response;

import com.deary.server.domain.correction.entity.CorrectionReactionType;
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
		List<Item> items,
		@Schema(description = "내가 이 교정에 보낸 반응 종류 (예: THANKS)")
		List<CorrectionReactionType> reactions
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
