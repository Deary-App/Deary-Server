package com.deary.server.domain.home.dto.response;

public record HomeFindResponse(
		String nickname,
		String profileImageUrl,
		boolean hasUnreadCorrection
) {
}
