package com.deary.server.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppleLoginRequest(
		@NotBlank(message = "Apple identity token을 입력해 주세요.")
		String identityToken,

		@NotBlank(message = "nonce를 입력해 주세요.")
		String nonce,

		@Schema(description = "Apple authorization code. 탈퇴 시 Apple 연결 해지에 쓰는 refresh token을 받는 데 사용한다. 없으면 로그인만 한다.",
				requiredMode = Schema.RequiredMode.NOT_REQUIRED)
		@Size(max = 1024, message = "Apple authorization code가 너무 깁니다.")
		String authorizationCode
) {
}
