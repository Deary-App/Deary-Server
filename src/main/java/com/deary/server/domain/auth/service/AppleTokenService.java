package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.client.AppleTokenClient;
import com.deary.server.domain.auth.client.SocialUserInfo;
import com.deary.server.domain.user.entity.SocialAccount;
import com.deary.server.domain.user.entity.SocialProvider;
import com.deary.server.domain.user.repository.SocialAccountRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Sign in with Apple refresh token을 저장하고, 탈퇴 시 Apple 연결을 해지한다.
 * 어떤 실패도 로그인·탈퇴를 막지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppleTokenService {

	private final AppleTokenClient appleTokenClient;
	private final AppleTokenCipher appleTokenCipher;
	private final SocialAccountRepository socialAccountRepository;

	public void registerAppleToken(SocialUserInfo userInfo, String authorizationCode) {
		if (authorizationCode == null || authorizationCode.isBlank() || !appleTokenClient.configured()) {
			return;
		}
		if (userInfo.clientId() == null) {
			log.warn("Apple identity token에 aud가 없어 refresh token 저장을 건너뜁니다.");
			return;
		}
		try {
			appleTokenClient.exchangeAuthorizationCode(authorizationCode, userInfo.clientId())
					.ifPresent(refreshToken -> socialAccountRepository.updateAppleTokenByProviderUserId(
							userInfo.providerUserId(),
							appleTokenCipher.encrypt(refreshToken),
							userInfo.clientId(),
							Instant.now()
					));
		} catch (RuntimeException exception) {
			log.warn("Apple refresh token 저장 실패: reason={}", exception.getClass().getSimpleName());
		}
	}

	/**
	 * 사용자 삭제 전에 호출한다. 저장된 토큰을 미리 읽고, 트랜잭션이 커밋된 뒤 Apple에 해지를 요청한다.
	 */
	public void removeAppleAuthorization(Long userId) {
		Optional<AppleCredential> credential = findAppleCredential(userId);
		if (credential.isEmpty()) {
			return;
		}
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					revoke(userId, credential.get());
				}
			});
			return;
		}
		revoke(userId, credential.get());
	}

	private Optional<AppleCredential> findAppleCredential(Long userId) {
		Optional<SocialAccount> account = socialAccountRepository
				.findSocialAccountByProviderAndUserId(SocialProvider.APPLE, userId);
		if (account.isEmpty()) {
			return Optional.empty();
		}
		if (!appleTokenClient.configured()) {
			log.warn("Apple 토큰 해지 설정이 비어 있어 연결 해지를 건너뜁니다: userId={}", userId);
			return Optional.empty();
		}
		SocialAccount socialAccount = account.get();
		if (socialAccount.getAppleRefreshToken() == null || socialAccount.getAppleClientId() == null) {
			log.warn("저장된 Apple refresh token이 없어 연결 해지를 건너뜁니다: userId={}", userId);
			return Optional.empty();
		}
		try {
			return Optional.of(new AppleCredential(
					appleTokenCipher.decrypt(socialAccount.getAppleRefreshToken()),
					socialAccount.getAppleClientId()
			));
		} catch (IllegalStateException exception) {
			log.warn("Apple refresh token을 복호화하지 못해 연결 해지를 건너뜁니다: userId={}", userId);
			return Optional.empty();
		}
	}

	private void revoke(Long userId, AppleCredential credential) {
		try {
			if (appleTokenClient.revokeRefreshToken(credential.refreshToken(), credential.clientId())) {
				log.info("Apple 연결 해지 완료: userId={}", userId);
			}
		} catch (RuntimeException exception) {
			log.warn("Apple 연결 해지 실패: userId={}, reason={}", userId, exception.getClass().getSimpleName());
		}
	}

	private record AppleCredential(String refreshToken, String clientId) {

		@Override
		public String toString() {
			return "AppleCredential[clientId=" + clientId + "]";
		}

	}

}
