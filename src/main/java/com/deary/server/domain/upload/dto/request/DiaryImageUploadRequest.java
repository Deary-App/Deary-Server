package com.deary.server.domain.upload.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DiaryImageUploadRequest(
		@NotNull(message = "사진 장수를 입력해 주세요.")
		@Min(value = 1, message = "사진은 1장 이상 올려 주세요.")
		@Max(value = 3, message = "사진은 최대 3장까지 올릴 수 있습니다.")
		Integer count
) {
}
