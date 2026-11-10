package com.deary.server.domain.diary.dto.response;

import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;

public record DiaryAuthorResponse(
		Long userId,
		String nickname,
		String profileImageUrl,
		String nationality,
		Language learningLanguage,
		LearningLevel learningLevel
) {
}
