package com.deary.server.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AppleLoginRequest(
		@NotBlank(message = "Apple identity token을 입력해 주세요.")
		String identityToken,

		@NotBlank(message = "nonce를 입력해 주세요.")
		String nonce
) {
}
