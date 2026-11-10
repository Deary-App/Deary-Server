package com.deary.server.domain.terms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ConsentSaveRequest(
		@NotEmpty(message = "동의할 약관을 하나 이상 보내 주세요.")
		List<@Valid @NotNull Agreement> agreements
) {

	public record Agreement(
			@NotNull(message = "약관 ID를 입력해 주세요.")
			Long termsId,

			@NotNull(message = "동의 여부를 입력해 주세요.")
			Boolean agreed
	) {
	}

}
