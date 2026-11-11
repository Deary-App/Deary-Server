package com.deary.server.domain.terms.service;

import com.deary.server.domain.terms.dto.request.ConsentSaveRequest;
import com.deary.server.domain.terms.dto.response.ConsentFindAllResponse;
import com.deary.server.domain.terms.dto.response.TermsFindAllResponse;
import com.deary.server.domain.terms.dto.response.TermsFindResponse;
import com.deary.server.domain.terms.entity.Terms;
import com.deary.server.domain.terms.entity.UserConsent;
import com.deary.server.domain.terms.exception.TermsErrorCode;
import com.deary.server.domain.terms.repository.TermsRepository;
import com.deary.server.domain.terms.repository.UserConsentRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CustomException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermsService {

	private final TermsRepository termsRepository;
	private final UserConsentRepository userConsentRepository;
	private final UserService userService;

	@Transactional(readOnly = true)
	public TermsFindAllResponse findAllCurrentTerms() {
		return TermsFindAllResponse.from(termsRepository.findAllCurrentTerms());
	}

	@Transactional(readOnly = true)
	public TermsFindResponse findTerms(Long termsId) {
		return termsRepository.findById(termsId)
				.map(TermsFindResponse::from)
				.orElseThrow(() -> new CustomException(TermsErrorCode.TERMS_NOT_FOUND));
	}

	@Transactional
	public ConsentFindAllResponse registerConsents(Long userId, ConsentSaveRequest request) {
		User user = userService.findUser(userId);
		Map<Long, Terms> currentTerms = currentTermsById();
		Set<Long> requestedTermsIds = new HashSet<>();
		for (ConsentSaveRequest.Agreement agreement : request.agreements()) {
			if (!requestedTermsIds.add(agreement.termsId())) {
				throw new CustomException(TermsErrorCode.DUPLICATE_TERMS);
			}
			Terms terms = currentTerms.get(agreement.termsId());
			if (terms == null) {
				throw new CustomException(TermsErrorCode.TERMS_NOT_CURRENT);
			}
			if (terms.isRequired() && !agreement.agreed()) {
				throw new CustomException(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
			}
			userConsentRepository.save(UserConsent.create(agreement.agreed(), user, terms));
		}
		ConsentFindAllResponse response = findAllConsent(userId);
		if (!response.requiredTermsAgreed()) {
			throw new CustomException(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
		}
		return response;
	}

	@Transactional(readOnly = true)
	public ConsentFindAllResponse findAllConsent(Long userId) {
		Map<Long, UserConsent> latestConsents = userConsentRepository.findAllLatestUserConsentByUserId(userId)
				.stream()
				.collect(Collectors.toMap(consent -> consent.getTerms().getId(), Function.identity()));
		List<ConsentFindAllResponse.ConsentStatus> consents = termsRepository.findAllCurrentTerms().stream()
				.map(terms -> toConsentStatus(terms, latestConsents.get(terms.getId())))
				.toList();
		boolean requiredTermsAgreed = consents.stream()
				.filter(ConsentFindAllResponse.ConsentStatus::required)
				.allMatch(ConsentFindAllResponse.ConsentStatus::agreed);
		return new ConsentFindAllResponse(requiredTermsAgreed, consents);
	}

	@Transactional(readOnly = true)
	public boolean hasAgreedRequiredTerms(Long userId) {
		return findAllConsent(userId).requiredTermsAgreed();
	}

	private Map<Long, Terms> currentTermsById() {
		return termsRepository.findAllCurrentTerms().stream()
				.collect(Collectors.toMap(Terms::getId, Function.identity()));
	}

	private ConsentFindAllResponse.ConsentStatus toConsentStatus(Terms terms, UserConsent consent) {
		return new ConsentFindAllResponse.ConsentStatus(
				terms.getId(),
				terms.getType(),
				terms.getVersion(),
				terms.getTitle(),
				terms.isRequired(),
				consent != null && consent.isAgreed(),
				consent == null ? null : consent.getCreatedAt()
		);
	}

}
