package com.deary.server.domain.terms.dto.response;

import com.deary.server.domain.terms.entity.Terms;
import com.deary.server.domain.terms.entity.TermsType;

public record TermsFindResponse(
		Long termsId,
		TermsType type,
		String version,
		String title,
		String content,
		boolean required
) {

	public static TermsFindResponse from(Terms terms) {
		return new TermsFindResponse(
				terms.getId(), terms.getType(), terms.getVersion(), terms.getTitle(), terms.getContent(),
				terms.isRequired());
	}

}
