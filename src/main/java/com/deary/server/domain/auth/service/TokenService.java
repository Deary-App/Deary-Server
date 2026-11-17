package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.dto.response.TokenIssueResponse;
import com.deary.server.domain.auth.entity.RefreshToken;
import com.deary.server.domain.auth.exception.AuthErrorCode;
import com.deary.server.domain.auth.repository.RefreshTokenRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import com.deary.server.global.jwt.AccessTokenProvider;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.jwt.JwtProperties;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

	private final AccessTokenProvider accessTokenProvider;
	private final RefreshTokenHasher refreshTokenHasher;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtProperties jwtProperties;

	@Transactional
	public TokenIssueResponse generateTokens(Long userId) {
		User user = userRepository.getReferenceById(userId);
		String rawRefreshToken = refreshTokenHasher.generateRawToken();
		RefreshToken refreshToken = refreshTokenRepository.save(
				RefreshToken.create(refreshTokenHasher.hash(rawRefreshToken), newRefreshTokenExpiresAt(), user)
		);
		return toTokenIssueResponse(userId, refreshToken.getId(), rawRefreshToken);
	}

	@Transactional(noRollbackFor = CustomException.class)
	public TokenIssueResponse handleReissue(String rawRefreshToken) {
		String oldTokenHash = refreshTokenHasher.hash(rawRefreshToken);
		RefreshToken refreshToken = refreshTokenRepository
				.findRefreshTokenByTokenHash(oldTokenHash)
				.orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));
		Long userId = refreshToken.getUser().getId();
		Long sessionId = refreshToken.getId();
		Instant now = Instant.now();
		if (refreshToken.isExpired(now)) {
			refreshTokenRepository.deleteExpiredRefreshToken(sessionId, oldTokenHash, now);
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
		String newRawRefreshToken = refreshTokenHasher.generateRawToken();
		int updated = refreshTokenRepository.updateRefreshTokenHash(
				sessionId, oldTokenHash, refreshTokenHasher.hash(newRawRefreshToken), newRefreshTokenExpiresAt(), now
		);
		if (updated == 0) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
		return toTokenIssueResponse(userId, sessionId, newRawRefreshToken);
	}

	@Transactional
	/**
	 * 리프레시 토큰을 지우고 그 주인의 ID를 돌려준다. 없는 토큰이면 비어 있다.
	 */
	public Optional<Long> removeRefreshToken(String rawRefreshToken) {
		String tokenHash = refreshTokenHasher.hash(rawRefreshToken);
		Optional<Long> userId = refreshTokenRepository.findRefreshTokenByTokenHash(tokenHash)
				.map(refreshToken -> refreshToken.getUser().getId());
		refreshTokenRepository.deleteRefreshTokenByTokenHash(tokenHash);
		return userId;
	}

	private Instant newRefreshTokenExpiresAt() {
		return Instant.now().plus(jwtProperties.refreshTokenExpiration());
	}

	private TokenIssueResponse toTokenIssueResponse(Long userId, Long sessionId, String rawRefreshToken) {
		return new TokenIssueResponse(
				accessTokenProvider.generateAccessToken(userId, sessionId),
				rawRefreshToken,
				jwtProperties.accessTokenExpiration().toSeconds(),
				jwtProperties.refreshTokenExpiration().toSeconds()
		);
	}

}
