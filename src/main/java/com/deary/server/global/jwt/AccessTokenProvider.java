package com.deary.server.global.jwt;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccessTokenProvider {

	public static final String SESSION_ID_CLAIM = "sid";

	private final JwtEncoder jwtEncoder;
	private final JwtProperties jwtProperties;

	public String generateAccessToken(Long userId, Long sessionId) {
		Instant now = Instant.now();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.subject(String.valueOf(userId))
				.issuedAt(now)
				.expiresAt(now.plus(jwtProperties.accessTokenExpiration()))
				.claim(SESSION_ID_CLAIM, String.valueOf(sessionId))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

}
