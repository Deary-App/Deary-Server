package com.deary.server.domain.diary.dto.response;

public record DiaryCreateResponse(
		Long diaryId,
		String firstPhotoUrl
) {
}
