package com.deary.server.domain.correction.event;

/**
 * 사람 교정이 제출됐다. 일기 작성자에게 교정 도착 알림을 보낸다.
 */
public record CorrectionCompletedEvent(
		Long diaryId,
		Long authorId,
		Long correctorId
) {
}
