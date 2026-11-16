package com.deary.server.domain.correction.dto.response;

import com.deary.server.domain.correction.entity.CorrectionReactionType;
import java.time.Instant;

public record CorrectionReactionCreateResponse(
		Long reactionId,
		CorrectionReactionType type,
		Instant createdAt
) {
}
