package com.deary.server.domain.correction.event;

import com.deary.server.domain.correction.entity.CorrectionReactionType;

/**
 * 일기 작성자가 교정에 반응(고마워요)을 처음 보냈다. 교정한 사람에게 알림을 보낸다.
 */
public record CorrectionReactionCreatedEvent(
		Long diaryId,
		Long correctorId,
		Long senderId,
		CorrectionReactionType type
) {
}
