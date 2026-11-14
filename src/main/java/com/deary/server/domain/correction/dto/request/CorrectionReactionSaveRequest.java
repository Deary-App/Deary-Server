package com.deary.server.domain.correction.dto.request;

import com.deary.server.domain.correction.entity.CorrectionReactionType;
import jakarta.validation.constraints.NotNull;

public record CorrectionReactionSaveRequest(
		@NotNull(message = "반응 종류를 선택해 주세요.")
		CorrectionReactionType type
) {
}
