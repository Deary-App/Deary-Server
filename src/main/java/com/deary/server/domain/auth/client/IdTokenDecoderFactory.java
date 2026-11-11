package com.deary.server.domain.auth.client;

import java.util.Collection;
import java.util.List;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

final class IdTokenDecoderFactory {

	private IdTokenDecoderFactory() {
	}

	static JwtDecoder create(String jwkSetUri, Collection<String> issuers, Collection<String> audiences) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		List<String> allowedAudiences = audiences.stream().filter(audience -> !audience.isBlank()).toList();
		OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
				new JwtTimestampValidator(),
				new JwtClaimValidator<String>(JwtClaimNames.ISS, issuers::contains),
				new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
						audience -> audience != null && audience.stream().anyMatch(allowedAudiences::contains))
		);
		decoder.setJwtValidator(validator);
		return decoder;
	}

}
