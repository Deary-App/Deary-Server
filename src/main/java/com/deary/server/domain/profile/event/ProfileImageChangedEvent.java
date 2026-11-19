package com.deary.server.domain.profile.event;

/**
 * 프로필 사진이 새로 정해졌다. AI 자동 검수가 사진을 검사한다.
 */
public record ProfileImageChangedEvent(
		Long userId
) {
}
