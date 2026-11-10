package com.deary.server.domain.terms.dto.response;

import com.deary.server.domain.terms.entity.Terms;
import com.deary.server.domain.terms.entity.TermsType;
import java.util.List;

public record TermsFindAllResponse(
		List<TermsSummary> terms
) {

	public static TermsFindAllResponse from(List<Terms> terms) {
		return new TermsFindAllResponse(terms.stream().map(TermsSummary::from).toList());
	}

	public record TermsSummary(
			Long termsId,
			TermsType type,
			String version,
			String title,
			boolean required
	) {

		public static TermsSummary from(Terms terms) {
			return new TermsSummary(
					terms.getId(), terms.getType(), terms.getVersion(), terms.getTitle(), terms.isRequired());
		}

	}

}
