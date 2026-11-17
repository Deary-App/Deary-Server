package com.deary.server.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LogoutRequest(
		@NotBlank(message = "리프레시 토큰을 입력해 주세요.")
		String refreshToken,

		@Schema(description = "이 기기의 Expo 푸시 토큰(선택). 보내면 로그아웃할 때 함께 지워 이 기기로 알림이 가지 않게 한다.",
				example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
		@Size(max = 255, message = "푸시 토큰은 255자 이하여야 합니다.")
		String pushToken
) {
}
