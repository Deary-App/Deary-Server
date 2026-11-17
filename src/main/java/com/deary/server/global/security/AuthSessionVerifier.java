package com.deary.server.global.security;

public interface AuthSessionVerifier {

	boolean isActiveSession(Long userId, Long sessionId);

}
