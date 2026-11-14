package com.deary.server.domain.home.service;

import com.deary.server.domain.correction.service.CorrectionService;
import com.deary.server.domain.home.dto.response.HomeFindResponse;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeService {

	private final ProfileService profileService;
	private final CorrectionService correctionService;

	@Transactional(readOnly = true)
	public HomeFindResponse findHome(Long userId) {
		Profile profile = profileService.findProfile(userId);
		return new HomeFindResponse(
				profile.getNickname(),
				profileService.profileImageUrl(profile),
				correctionService.hasUnreadCorrection(userId),
				correctionService.hasUnreadCorrectionReaction(userId)
		);
	}

}
