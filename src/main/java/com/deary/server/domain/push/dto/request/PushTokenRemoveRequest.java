package com.deary.server.domain.push.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PushTokenRemoveRequest(
		@Schema(description = "지울 Expo 푸시 토큰", example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
		@NotBlank(message = "푸시 토큰을 보내 주세요.")
		String token
) {
}
