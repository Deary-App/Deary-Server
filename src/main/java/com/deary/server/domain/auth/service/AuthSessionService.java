package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.repository.RefreshTokenRepository;
import com.deary.server.global.security.AuthSessionVerifier;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthSessionService implements AuthSessionVerifier {

	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	@Transactional(readOnly = true)
	public boolean isActiveSession(Long userId, Long sessionId) {
		return refreshTokenRepository.existsByIdAndUser_IdAndExpiresAtAfter(sessionId, userId, Instant.now());
	}

}
