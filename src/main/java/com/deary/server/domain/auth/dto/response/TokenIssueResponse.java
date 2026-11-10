package com.deary.server.domain.auth.dto.response;

public record TokenIssueResponse(
		String accessToken,
		String refreshToken,
		long accessTokenExpiresIn,
		long refreshTokenExpiresIn
) {
}
