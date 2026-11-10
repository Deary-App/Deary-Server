package com.deary.server.domain.profile.dto.response;

import com.deary.server.domain.profile.entity.Gender;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;
import com.deary.server.domain.profile.entity.Profile;

public record PublicProfileFindResponse(
		Long userId,
		String nickname,
		String profileImagePath,
		String nationality,
		Language nativeLanguage,
		Language learningLanguage,
		LearningLevel learningLevel,
		int age,
		Gender gender
) {

	public static PublicProfileFindResponse of(Profile profile, int age) {
		return new PublicProfileFindResponse(
				profile.getUserId(),
				profile.getNickname(),
				profile.getProfileImagePath(),
				profile.getNationality(),
				profile.getNativeLanguage(),
				profile.getLearningLanguage(),
				profile.getLearningLevel(),
				age,
				profile.getGender() == Gender.UNDISCLOSED ? null : profile.getGender()
		);
	}

}
