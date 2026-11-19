package com.deary.server.domain.correction.event;

/**
 * 사람 교정이 제출됐다. 일기 작성자에게 교정 도착 알림을 보내고, AI 자동 검수가 교정 내용을 검사한다.
 */
public record CorrectionCompletedEvent(
		Long correctionId,
		Long diaryId,
		Long authorId,
		Long correctorId
) {
}
