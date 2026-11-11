package com.deary.server.domain.diary.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DiarySaveRequest(
		@NotBlank(message = "제목을 입력해 주세요.")
		@Size(max = 80, message = "제목은 80자 이내로 입력해 주세요.")
		String title,

		@NotBlank(message = "본문을 입력해 주세요.")
		@Size(max = 2000, message = "본문은 2000자 이내로 입력해 주세요.")
		String content,

		@NotNull(message = "사진을 한 장 이상 붙여 주세요.")
		@Size(min = 1, max = 3, message = "사진은 1~3장 붙여 주세요.")
		List<@Valid @NotNull Photo> photos
) {

	public record Photo(
			@NotBlank(message = "사진 경로를 입력해 주세요.")
			String path,

			@NotNull(message = "사진 위치를 입력해 주세요.")
			@DecimalMin(value = "0.0", message = "사진 위치는 0~1 사이여야 합니다.")
			@DecimalMax(value = "1.0", message = "사진 위치는 0~1 사이여야 합니다.")
			Double positionX,

			@NotNull(message = "사진 위치를 입력해 주세요.")
			@DecimalMin(value = "0.0", message = "사진 위치는 0~1 사이여야 합니다.")
			@DecimalMax(value = "1.0", message = "사진 위치는 0~1 사이여야 합니다.")
			Double positionY,

			@NotNull(message = "사진 크기를 입력해 주세요.")
			@Positive(message = "사진 크기는 0보다 커야 합니다.")
			@DecimalMax(value = "10.0", message = "사진 크기가 너무 큽니다.")
			Double scale,

			@NotNull(message = "사진 회전을 입력해 주세요.")
			@DecimalMin(value = "-360.0", message = "사진 회전은 -360~360도 사이여야 합니다.")
			@DecimalMax(value = "360.0", message = "사진 회전은 -360~360도 사이여야 합니다.")
			Double rotation
	) {
	}

}
