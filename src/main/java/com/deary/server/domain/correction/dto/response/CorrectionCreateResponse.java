package com.deary.server.domain.correction.dto.response;

public record CorrectionCreateResponse(
		Long correctionId,
		Long diaryId,
		String authorProfileImageUrl
) {
}
