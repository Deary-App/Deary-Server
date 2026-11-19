package com.deary.server.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 운영자 API 키. 비어 있으면 운영자 API를 모두 거절한다.
 */
@ConfigurationProperties(prefix = "admin")
public record AdminKeyProperties(
		String apiKey
) {

	public boolean configured() {
		return apiKey != null && !apiKey.isBlank();
	}

}
