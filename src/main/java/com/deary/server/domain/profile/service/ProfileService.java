package com.deary.server.domain.profile.service;

import com.deary.server.domain.profile.dto.request.ProfileSaveRequest;
import com.deary.server.domain.profile.dto.response.ProfileFindResponse;
import com.deary.server.domain.profile.dto.response.PublicProfileFindResponse;
import com.deary.server.domain.profile.dto.response.RegistrationFindResponse;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.exception.ProfileErrorCode;
import com.deary.server.domain.profile.repository.ProfileRepository;
import com.deary.server.domain.terms.service.TermsService;
import com.deary.server.domain.upload.service.StoragePathPolicy;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
	private static final int MINIMUM_AGE = 14;
	private static final Set<String> COUNTRY_CODES = Set.of(Locale.getISOCountries());

	private final ProfileRepository profileRepository;
	private final UserService userService;
	private final TermsService termsService;
	private final StoragePathPolicy storagePathPolicy;
	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;

	@Transactional
	public ProfileFindResponse handleProfileSave(Long userId, ProfileSaveRequest request) {
		User user = userService.findUser(userId);
		if (!termsService.hasAgreedRequiredTerms(userId)) {
			throw new CustomException(ProfileErrorCode.TERMS_NOT_AGREED);
		}
		validate(userId, request);
		Profile.ProfileInfo info = toProfileInfo(request);
		Optional<Profile> existingProfile = profileRepository.findById(userId);
		if (existingProfile.isEmpty()) {
			Profile profile = profileRepository.saveAndFlush(Profile.create(user, info));
			return ProfileFindResponse.of(profile, user.getEmail(), profileImageUrl(profile));
		}
		Profile profile = existingProfile.get();
		if (request.version() == null) {
			throw new CustomException(ProfileErrorCode.VERSION_REQUIRED);
		}
		if (!request.version().equals(profile.getVersion())) {
			throw new CustomException(ProfileErrorCode.VERSION_CONFLICT);
		}
		profile.modify(info);
		profileRepository.flush();
		return ProfileFindResponse.of(profile, user.getEmail(), profileImageUrl(profile));
	}

	@Transactional(readOnly = true)
	public ProfileFindResponse findMyProfile(Long userId) {
		User user = userService.findUser(userId);
		Profile profile = profileRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));
		return ProfileFindResponse.of(profile, user.getEmail(), profileImageUrl(profile));
	}

	@Transactional(readOnly = true)
	public Profile findProfile(Long userId) {
		return profileRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public RegistrationFindResponse findRegistration(Long userId) {
		userService.findUser(userId);
		return new RegistrationFindResponse(
				termsService.hasAgreedRequiredTerms(userId),
				profileRepository.existsById(userId)
		);
	}

	@Transactional(readOnly = true)
	public PublicProfileFindResponse findPublicProfile(Long userId) {
		Profile profile = profileRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));
		return PublicProfileFindResponse.of(profile, profileImageUrl(profile), ageOf(profile.getBirthDate()));
	}

	private void validate(Long userId, ProfileSaveRequest request) {
		if (request.profileImagePath() != null
				&& !storagePathPolicy.isOwnedProfileImagePath(userId, request.profileImagePath())) {
			throw new CustomException(ProfileErrorCode.INVALID_PROFILE_IMAGE);
		}
		if (ageOf(request.birthDate()) < MINIMUM_AGE) {
			throw new CustomException(ProfileErrorCode.UNDER_MINIMUM_AGE);
		}
		if (request.nativeLanguage() == request.learningLanguage()) {
			throw new CustomException(ProfileErrorCode.SAME_LANGUAGE);
		}
		if (!COUNTRY_CODES.contains(request.nationality())) {
			throw new CustomException(ProfileErrorCode.INVALID_NATIONALITY);
		}
	}

	private String profileImageUrl(Profile profile) {
		return storageClient.publicUrl(storageProperties.profileBucket(), profile.getProfileImagePath());
	}

	private int ageOf(LocalDate birthDate) {
		return Period.between(birthDate, LocalDate.now(SERVICE_ZONE)).getYears();
	}

	private Profile.ProfileInfo toProfileInfo(ProfileSaveRequest request) {
		return new Profile.ProfileInfo(
				request.nickname().strip(),
				request.birthDate(),
				request.nationality(),
				request.nativeLanguage(),
				request.learningLanguage(),
				request.learningLevel(),
				request.gender(),
				request.profileImagePath()
		);
	}

}
