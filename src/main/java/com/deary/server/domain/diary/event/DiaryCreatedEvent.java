package com.deary.server.domain.diary.event;

/**
 * 일기가 저장됐다. AI 자동 검수가 글과 사진을 검사한다.
 */
public record DiaryCreatedEvent(
		Long diaryId
) {
}
