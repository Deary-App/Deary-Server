package com.deary.server.domain.block.dto.response;

import java.time.Instant;
import java.util.List;

public record BlockFindAllResponse(
		List<BlockedUser> blocks
) {

	public record BlockedUser(
			Long userId,
			String nickname,
			String profileImageUrl,
			Instant blockedAt
	) {
	}

}
