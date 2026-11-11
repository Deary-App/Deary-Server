package com.deary.server.domain.correction.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CorrectionSaveRequest(
		@NotEmpty(message = "문장별 교정을 보내 주세요.")
		List<@Valid @NotNull Item> items,

		@NotBlank(message = "전체 코멘트를 입력해 주세요.")
		@Size(max = 1000, message = "전체 코멘트는 1000자 이내로 입력해 주세요.")
		String comment
) {

	@Schema(name = "CorrectionSaveItem")
	public record Item(
			@NotNull(message = "문장 ID를 입력해 주세요.")
			Long sentenceId,

			@NotNull(message = "고쳤는지 여부를 입력해 주세요.")
			Boolean changed,

			@Size(max = 2000, message = "고친 문장은 2000자 이내로 입력해 주세요.")
			String correctedContent,

			@Size(max = 500, message = "고친 이유는 500자 이내로 입력해 주세요.")
			String reason
	) {
	}

}
