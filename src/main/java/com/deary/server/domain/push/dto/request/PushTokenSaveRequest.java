package com.deary.server.domain.push.dto.request;

import com.deary.server.domain.push.entity.PushLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushTokenSaveRequest(
		@Schema(description = "Expo 푸시 토큰", example = "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]")
		@NotBlank(message = "푸시 토큰을 보내 주세요.")
		@Size(max = 255, message = "푸시 토큰은 255자 이하여야 합니다.")
		@Pattern(regexp = "^Expo(nent)?PushToken\\[[^\\]]+\\]$", message = "Expo 푸시 토큰 형식이 아닙니다.")
		String token,

		@Schema(description = "알림 문구 언어(기기 언어)", example = "KO")
		@NotNull(message = "알림 언어를 보내 주세요.")
		PushLanguage language
) {
}
