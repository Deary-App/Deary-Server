package com.deary.server.domain.profile.dto.response;

import com.deary.server.domain.profile.entity.Gender;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;
import com.deary.server.domain.profile.entity.Profile;
import java.time.LocalDate;

public record ProfileFindResponse(
		Long userId,
		String email,
		String nickname,
		LocalDate birthDate,
		String nationality,
		Language nativeLanguage,
		Language learningLanguage,
		LearningLevel learningLevel,
		Gender gender,
		String profileImagePath,
		String profileImageUrl,
		Long version
) {

	public static ProfileFindResponse of(Profile profile, String email, String profileImageUrl) {
		return new ProfileFindResponse(
				profile.getUserId(),
				email,
				profile.getNickname(),
				profile.getBirthDate(),
				profile.getNationality(),
				profile.getNativeLanguage(),
				profile.getLearningLanguage(),
				profile.getLearningLevel(),
				profile.getGender(),
				profile.getProfileImagePath(),
				profileImageUrl,
				profile.getVersion()
		);
	}

}
