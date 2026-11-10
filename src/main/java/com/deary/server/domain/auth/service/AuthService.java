package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.client.SocialIdTokenVerifier;
import com.deary.server.domain.auth.client.SocialUserInfo;
import com.deary.server.domain.auth.dto.request.AppleLoginRequest;
import com.deary.server.domain.auth.dto.request.GoogleLoginRequest;
import com.deary.server.domain.auth.dto.response.SocialLoginResponse;
import com.deary.server.domain.user.entity.SocialAccount;
import com.deary.server.domain.user.repository.SocialAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final SocialIdTokenVerifier socialIdTokenVerifier;
	private final SocialAccountRepository socialAccountRepository;
	private final SocialAccountRegistrar socialAccountRegistrar;
	private final TokenService tokenService;

	public SocialLoginResponse handleAppleLogin(AppleLoginRequest request) {
		return login(socialIdTokenVerifier.verifyApple(request.identityToken(), request.nonce()));
	}

	public SocialLoginResponse handleGoogleLogin(GoogleLoginRequest request) {
		return login(socialIdTokenVerifier.verifyGoogle(request.idToken()));
	}

	private SocialLoginResponse login(SocialUserInfo userInfo) {
		Optional<Long> existingUserId = findUserId(userInfo);
		if (existingUserId.isPresent()) {
			return SocialLoginResponse.of(tokenService.generateTokens(existingUserId.get()), false);
		}
		try {
			Long userId = socialAccountRegistrar.register(userInfo);
			return SocialLoginResponse.of(tokenService.generateTokens(userId), true);
		} catch (DataIntegrityViolationException exception) {
			Long userId = findUserId(userInfo).orElseThrow(() -> exception);
			return SocialLoginResponse.of(tokenService.generateTokens(userId), false);
		}
	}

	private Optional<Long> findUserId(SocialUserInfo userInfo) {
		return socialAccountRepository
				.findSocialAccountByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
				.map(SocialAccount::getUser)
				.map(user -> user.getId());
	}

}
