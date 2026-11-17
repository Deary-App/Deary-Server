package com.deary.server.global.security;

import com.deary.server.global.jwt.AccessTokenProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUserAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final AuthSessionVerifier authSessionVerifier;

	@Override
	public AbstractAuthenticationToken convert(Jwt jwt) {
		Long userId = parseId(jwt.getSubject());
		Long sessionId = parseId(jwt.getClaimAsString(AccessTokenProvider.SESSION_ID_CLAIM));
		if (!authSessionVerifier.isActiveSession(userId, sessionId)) {
			throw new InvalidBearerTokenException("유효하지 않은 토큰입니다.");
		}
		AuthUser authUser = new AuthUser(userId);
		return new UsernamePasswordAuthenticationToken(authUser, jwt, List.of());
	}

	private Long parseId(String value) {
		if (value == null) {
			throw new InvalidBearerTokenException("유효하지 않은 토큰입니다.");
		}
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException exception) {
			throw new InvalidBearerTokenException("유효하지 않은 토큰입니다.");
		}
	}

}
