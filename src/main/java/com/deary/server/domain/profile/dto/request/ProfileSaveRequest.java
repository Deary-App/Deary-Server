package com.deary.server.domain.profile.dto.request;

import com.deary.server.domain.profile.entity.Gender;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ProfileSaveRequest(
		@NotBlank(message = "닉네임을 입력해 주세요.")
		@Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해 주세요.")
		String nickname,

		@NotNull(message = "생년월일을 입력해 주세요.")
		@Past(message = "생년월일을 다시 확인해 주세요.")
		LocalDate birthDate,

		@NotBlank(message = "국적을 선택해 주세요.")
		@Pattern(regexp = "^[A-Z]{2}$", message = "국적은 국가 코드 2자리로 보내 주세요.")
		String nationality,

		@NotNull(message = "모국어를 선택해 주세요.")
		Language nativeLanguage,

		@NotNull(message = "학습 언어를 선택해 주세요.")
		Language learningLanguage,

		@NotNull(message = "학습 언어 수준을 선택해 주세요.")
		LearningLevel learningLevel,

		@NotNull(message = "성별을 선택해 주세요.")
		Gender gender,

		@Size(max = 255, message = "프로필 사진 경로가 너무 깁니다.")
		String profileImagePath,

		Long version
) {
}
