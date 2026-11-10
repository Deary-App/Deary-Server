package com.deary.server.domain.profile.dto.response;

public record RegistrationFindResponse(
		boolean termsAgreed,
		boolean profileCompleted
) {
}
