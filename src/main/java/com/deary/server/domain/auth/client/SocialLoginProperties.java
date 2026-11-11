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
			List<String> clientIds
	) {
	}

	public record Google(
			List<String> issuers,
			String jwkSetUri,
			List<String> clientIds
	) {
	}

}
