package com.deary.server.domain.auth.dto.response;

public record SocialLoginResponse(
		String accessToken,
		String refreshToken,
		long accessTokenExpiresIn,
		long refreshTokenExpiresIn,
		boolean newUser
) {

	public static SocialLoginResponse of(TokenIssueResponse tokens, boolean newUser) {
		return new SocialLoginResponse(
				tokens.accessToken(),
				tokens.refreshToken(),
				tokens.accessTokenExpiresIn(),
				tokens.refreshTokenExpiresIn(),
				newUser
		);
	}

}
