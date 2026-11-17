package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.client.SocialIdTokenVerifier;
import com.deary.server.domain.auth.client.SocialUserInfo;
import com.deary.server.domain.auth.dto.request.AppleLoginRequest;
import com.deary.server.domain.auth.dto.request.GoogleLoginRequest;
import com.deary.server.domain.auth.dto.request.LogoutRequest;
import com.deary.server.domain.auth.dto.request.TokenReissueRequest;
import com.deary.server.domain.auth.dto.response.TokenIssueResponse;
import com.deary.server.domain.auth.dto.response.SocialLoginResponse;
import com.deary.server.domain.push.service.PushTokenService;
import com.deary.server.domain.user.entity.SocialAccount;
import com.deary.server.domain.user.repository.SocialAccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final SocialIdTokenVerifier socialIdTokenVerifier;
	private final SocialAccountRepository socialAccountRepository;
	private final SocialAccountRegistrar socialAccountRegistrar;
	private final TokenService tokenService;
	private final AppleTokenService appleTokenService;
	private final PushTokenService pushTokenService;

	public SocialLoginResponse handleAppleLogin(AppleLoginRequest request) {
		SocialUserInfo userInfo = socialIdTokenVerifier.verifyApple(request.identityToken(), request.nonce());
		SocialLoginResponse response = login(userInfo);
		appleTokenService.registerAppleToken(userInfo, request.authorizationCode());
		return response;
	}

	public SocialLoginResponse handleGoogleLogin(GoogleLoginRequest request) {
		return login(socialIdTokenVerifier.verifyGoogle(request.idToken()));
	}

	public TokenIssueResponse handleReissue(TokenReissueRequest request) {
		return tokenService.handleReissue(request.refreshToken());
	}

	/**
	 * 이 기기의 리프레시 토큰을 지운다. 푸시 토큰도 보냈으면 리프레시 토큰 주인의 것일 때만 함께 지운다.
	 * 두 삭제는 한 트랜잭션이라 하나가 실패하면 둘 다 되돌린다.
	 */
	@Transactional
	public void handleLogout(LogoutRequest request) {
		Optional<Long> userId = tokenService.removeRefreshToken(request.refreshToken());
		if (userId.isPresent() && request.pushToken() != null && !request.pushToken().isBlank()) {
			pushTokenService.removePushToken(userId.get(), request.pushToken());
		}
	}

	private SocialLoginResponse login(SocialUserInfo userInfo) {
		Optional<Long> existingUserId = findUserId(userInfo);
		if (existingUserId.isPresent()) {
			return SocialLoginResponse.of(tokenService.generateTokens(existingUserId.get()), false);
		}
		Long registeredUserId;
		try {
			registeredUserId = socialAccountRegistrar.register(userInfo);
		} catch (DataIntegrityViolationException exception) {
			Long userId = findUserId(userInfo).orElseThrow(() -> exception);
			return SocialLoginResponse.of(tokenService.generateTokens(userId), false);
		}
		return SocialLoginResponse.of(tokenService.generateTokens(registeredUserId), true);
	}

	private Optional<Long> findUserId(SocialUserInfo userInfo) {
		return socialAccountRepository
				.findSocialAccountByProviderAndProviderUserId(userInfo.provider(), userInfo.providerUserId())
				.map(SocialAccount::getUser)
				.map(user -> user.getId());
	}

}
