package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.dto.response.TokenIssueResponse;
import com.deary.server.domain.auth.entity.RefreshToken;
import com.deary.server.domain.auth.repository.RefreshTokenRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import com.deary.server.global.jwt.AccessTokenProvider;
import com.deary.server.global.jwt.JwtProperties;
import java.time.Instant;
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
		Instant refreshTokenExpiresAt = Instant.now().plus(jwtProperties.refreshTokenExpiration());
		refreshTokenRepository.save(
				RefreshToken.create(refreshTokenHasher.hash(rawRefreshToken), refreshTokenExpiresAt, user)
		);
		return new TokenIssueResponse(
				accessTokenProvider.generateAccessToken(userId),
				rawRefreshToken,
				jwtProperties.accessTokenExpiration().toSeconds(),
				jwtProperties.refreshTokenExpiration().toSeconds()
		);
	}

}
