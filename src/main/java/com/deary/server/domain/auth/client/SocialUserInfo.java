package com.deary.server.domain.auth.client;

import com.deary.server.domain.user.entity.SocialProvider;

public record SocialUserInfo(
		SocialProvider provider,
		String providerUserId,
		String email,
		String clientId
) {
}
