package com.deary.server.domain.auth.client;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record SocialLoginProperties(
		Apple apple,
		Google google
) {

	public record Apple(
			String issuer,
			String jwkSetUri,
			List<String> clientIds,
			String tokenUri,
			String revokeUri,
			String teamId,
			String keyId,
			String privateKey
	) {

		public boolean revokeConfigured() {
			return hasText(teamId) && hasText(keyId) && hasText(privateKey);
		}

		private static boolean hasText(String value) {
			return value != null && !value.isBlank();
		}

	}

	public record Google(
			List<String> issuers,
			String jwkSetUri,
			List<String> clientIds
	) {
	}

}
