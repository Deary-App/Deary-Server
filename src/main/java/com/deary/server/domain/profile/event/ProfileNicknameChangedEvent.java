package com.deary.server.domain.profile.event;

/**
 * 닉네임이 새로 정해졌다. AI 자동 검수가 닉네임을 검사한다.
 */
public record ProfileNicknameChangedEvent(
		Long userId
) {
}
