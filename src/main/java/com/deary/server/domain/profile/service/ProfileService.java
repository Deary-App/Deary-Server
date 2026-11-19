package com.deary.server.domain.profile.service;

import com.deary.server.domain.block.repository.UserBlockRepository;
import com.deary.server.domain.profile.dto.request.ProfileSaveRequest;
import com.deary.server.domain.profile.dto.response.ProfileFindResponse;
import com.deary.server.domain.profile.dto.response.PublicProfileFindResponse;
import com.deary.server.domain.profile.dto.response.RegistrationFindResponse;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.event.ProfileImageChangedEvent;
import com.deary.server.domain.profile.event.ProfileNicknameChangedEvent;
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
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

	private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");
	private static final int MINIMUM_AGE = 14;
	private static final int ADULT_AGE = 18;
	private static final Set<String> COUNTRY_CODES = Set.of(Locale.getISOCountries());

	private final ProfileRepository profileRepository;
	private final UserService userService;
	private final TermsService termsService;
	private final StoragePathPolicy storagePathPolicy;
	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;
	private final UserBlockRepository userBlockRepository;
	private final ApplicationEventPublisher eventPublisher;

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
			publishImageChanged(userId, null, profile.getProfileImagePath());
			publishNicknameChanged(userId, null, profile.getNickname());
			return ProfileFindResponse.of(profile, user.getEmail(), profileImageUrl(profile));
		}
		Profile profile = existingProfile.get();
		if (request.version() == null) {
			throw new CustomException(ProfileErrorCode.VERSION_REQUIRED);
		}
		if (!request.version().equals(profile.getVersion())) {
			throw new CustomException(ProfileErrorCode.VERSION_CONFLICT);
		}
		String previousImagePath = profile.getProfileImagePath();
		String previousNickname = profile.getNickname();
		profile.modify(info);
		profileRepository.flush();
		publishImageChanged(userId, previousImagePath, profile.getProfileImagePath());
		publishNicknameChanged(userId, previousNickname, profile.getNickname());
		return ProfileFindResponse.of(profile, user.getEmail(), profileImageUrl(profile));
	}

	private void publishImageChanged(Long userId, String previousImagePath, String imagePath) {
		if (imagePath != null && !imagePath.equals(previousImagePath)) {
			eventPublisher.publishEvent(new ProfileImageChangedEvent(userId));
		}
	}

	private void publishNicknameChanged(Long userId, String previousNickname, String nickname) {
		if (nickname != null && !nickname.equals(previousNickname)) {
			eventPublisher.publishEvent(new ProfileNicknameChangedEvent(userId));
		}
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
	public Map<Long, Profile> findAllProfileById(Collection<Long> userIds) {
		return profileRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(Profile::getUserId, Function.identity()));
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
	public PublicProfileFindResponse findPublicProfile(Long viewerId, Long userId) {
		Profile profile = profileRepository.findById(userId)
				.orElseThrow(() -> new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND));
		if (userBlockRepository.existsUserBlockBetween(viewerId, userId)
				|| (!viewerId.equals(userId) && profile.getUser().isSuspended())) {
			throw new CustomException(ProfileErrorCode.PROFILE_NOT_FOUND);
		}
		return PublicProfileFindResponse.of(profile, profileImageUrl(profile), publicAgeOf(profile.getBirthDate()));
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

	/**
	 * 운영자가 내린 프로필 사진은 본인을 포함해 누구에게도 내려주지 않는다.
	 */
	public String profileImageUrl(Profile profile) {
		if (profile.isProfileImageHidden()) {
			return null;
		}
		return storageClient.publicUrl(storageProperties.profileBucket(), profile.getProfileImagePath());
	}

	// 18세 미만이면 다른 사람에게 나이를 보여 주지 않는다.
	private Integer publicAgeOf(LocalDate birthDate) {
		int age = ageOf(birthDate);
		return age < ADULT_AGE ? null : age;
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
