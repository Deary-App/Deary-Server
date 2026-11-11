package com.deary.server.domain.correction.dto.response;

import com.deary.server.domain.diary.dto.response.DiaryAuthorResponse;
import java.time.Instant;
import java.util.List;

public record CorrectionClaimResponse(
		Long diaryId,
		String title,
		DiaryAuthorResponse author,
		Instant claimExpiresAt,
		List<SentenceResponse> sentences
) {

	public record SentenceResponse(
			Long sentenceId,
			int sentenceIndex,
			String content
	) {
	}

}
