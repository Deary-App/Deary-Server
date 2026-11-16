package com.deary.server.domain.push.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Expo 푸시 설정. accessToken은 Expo 프로젝트에서 '푸시 보안'을 켰을 때만 필요하다.
 */
@ConfigurationProperties(prefix = "push.expo")
public record ExpoPushProperties(
		boolean enabled,
		String accessToken
) {

	public boolean hasAccessToken() {
		return accessToken != null && !accessToken.isBlank();
	}

}
