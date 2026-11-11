package com.deary.server.domain.terms.dto.response;

import com.deary.server.domain.terms.entity.TermsType;
import java.time.Instant;
import java.util.List;

public record ConsentFindAllResponse(
		boolean requiredTermsAgreed,
		List<ConsentStatus> consents
) {

	public record ConsentStatus(
			Long termsId,
			TermsType type,
			String version,
			String title,
			boolean required,
			boolean agreed,
			Instant consentedAt
	) {
	}

}
