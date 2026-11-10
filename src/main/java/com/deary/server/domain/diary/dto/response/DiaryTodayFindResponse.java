package com.deary.server.domain.diary.dto.response;

import java.time.LocalDate;

public record DiaryTodayFindResponse(
		LocalDate today,
		boolean written,
		Long diaryId
) {
}
