package com.deary.server.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
		@NotBlank(message = "Google ID 토큰을 입력해 주세요.")
		String idToken
) {
}
