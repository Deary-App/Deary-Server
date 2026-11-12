package com.deary.server.domain.correction.dto.response;

import java.time.Instant;
import java.util.List;

public record PublicCorrectionResponse(
		Long correctionId,
		CorrectionFindResponse.Corrector corrector,
		String comment,
		Instant correctedAt,
		List<CorrectionFindResponse.Item> items
) {
}
