package com.deary.server.domain.correction.service;

import com.deary.server.domain.correction.dto.request.CorrectionSaveRequest;
import com.deary.server.domain.correction.dto.response.CorrectionClaimResponse;
import com.deary.server.domain.correction.dto.response.CorrectionCreateResponse;
import com.deary.server.domain.correction.dto.response.CorrectionFindAllResponse;
import com.deary.server.domain.correction.dto.response.CorrectionFindResponse;
import com.deary.server.domain.correction.dto.response.PublicCorrectionResponse;
import com.deary.server.domain.correction.entity.Correction;
import com.deary.server.domain.correction.entity.CorrectionBadge;
import com.deary.server.domain.correction.entity.CorrectionItem;
import com.deary.server.domain.correction.entity.CorrectionRequest;
import com.deary.server.domain.correction.entity.CorrectionRequestStatus;
import com.deary.server.domain.correction.exception.CorrectionErrorCode;
import com.deary.server.domain.correction.repository.CorrectionRepository;
import com.deary.server.domain.correction.repository.CorrectionRequestRepository;
import com.deary.server.domain.diary.dto.response.DiaryAuthorResponse;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.entity.DiarySentence;
import com.deary.server.domain.diary.exception.DiaryErrorCode;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CustomException;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Limit;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(CorrectionProperties.class)
public class CorrectionService {

	private static final int EXCERPT_LENGTH = 50;

	private final CorrectionRequestRepository correctionRequestRepository;
	private final CorrectionRepository correctionRepository;
	private final UserService userService;
	private final ProfileService profileService;
	private final CorrectionProperties correctionProperties;

	@Transactional
	public CorrectionClaimResponse handleClaim(Long userId, Long diaryId) {
		User corrector = userService.findUser(userId);
		profileService.findProfile(userId);
		CorrectionRequest request = findRequest(diaryId);
		Diary diary = request.getDiary();
		if (diary.isWrittenBy(userId)) {
			throw new CustomException(CorrectionErrorCode.OWN_DIARY);
		}
		if (request.getStatus() == CorrectionRequestStatus.COMPLETED) {
			throw new CustomException(CorrectionErrorCode.ALREADY_COMPLETED);
		}
		Instant now = Instant.now();
		if (request.isClaimActiveAt(now) && !request.isClaimedActiveBy(userId, now)) {
			throw new CustomException(CorrectionErrorCode.ALREADY_CLAIMED);
		}
		request.claim(corrector, now.plus(correctionProperties.claimDuration()));
		try {
			correctionRequestRepository.flush();
		} catch (ObjectOptimisticLockingFailureException exception) {
			throw new CustomException(CorrectionErrorCode.ALREADY_CLAIMED);
		}
		List<CorrectionClaimResponse.SentenceResponse> sentences = diary.getSentences().stream()
				.map(sentence -> new CorrectionClaimResponse.SentenceResponse(
						sentence.getId(), sentence.getSentenceIndex(), sentence.getContent()))
				.toList();
		return new CorrectionClaimResponse(
				diary.getId(),
				diary.getTitle(),
				toAuthor(diary.getUser().getId()),
				request.getClaimExpiresAt(),
				sentences
		);
	}

	@Transactional
	public void removeClaim(Long userId, Long diaryId) {
		CorrectionRequest request = findRequest(diaryId);
		if (request.isClaimedActiveBy(userId, Instant.now())) {
			request.release();
		}
	}

	@Transactional
	public CorrectionCreateResponse generateCorrection(Long userId, Long diaryId, CorrectionSaveRequest saveRequest) {
		User corrector = userService.findUser(userId);
		CorrectionRequest request = findRequest(diaryId);
		Instant now = Instant.now();
		if (request.getStatus() == CorrectionRequestStatus.COMPLETED) {
			throw new CustomException(CorrectionErrorCode.ALREADY_COMPLETED);
		}
		if (!request.isClaimedActiveBy(userId, now)) {
			throw new CustomException(CorrectionErrorCode.CLAIM_NOT_ACTIVE);
		}
		Diary diary = request.getDiary();
		Map<Long, DiarySentence> sentences = diary.getSentences().stream()
				.collect(Collectors.toMap(DiarySentence::getId, Function.identity()));
		validateItems(sentences, saveRequest.items());
		Correction correction = Correction.create(request, corrector, saveRequest.comment().strip());
		for (CorrectionSaveRequest.Item item : saveRequest.items()) {
			correction.addItem(
					sentences.get(item.sentenceId()),
					item.changed(),
					item.changed() ? item.correctedContent().strip() : null,
					blankToNull(item.reason())
			);
		}
		request.complete(now);
		try {
			correctionRepository.saveAndFlush(correction);
		} catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException exception) {
			throw new CustomException(CorrectionErrorCode.ALREADY_COMPLETED);
		}
		Profile author = profileService.findAllProfileById(List.of(diary.getUser().getId())).get(diary.getUser().getId());
		return new CorrectionCreateResponse(
				correction.getId(),
				diary.getId(),
				author == null ? null : profileService.profileImageUrl(author)
		);
	}

	@Transactional(readOnly = true)
	public CorrectionFindResponse findCorrectionOfMyDiary(Long userId, Long diaryId) {
		CorrectionRequest request = findRequest(diaryId);
		if (!request.getDiary().isWrittenBy(userId)) {
			throw new CustomException(DiaryErrorCode.DIARY_NOT_FOUND);
		}
		Correction correction = correctionRepository.findCorrectionWithItemsByDiaryId(diaryId)
				.orElseThrow(() -> new CustomException(CorrectionErrorCode.CORRECTION_NOT_FOUND));
		return new CorrectionFindResponse(
				correction.getId(),
				diaryId,
				toCorrector(correction),
				correction.getComment(),
				correction.getCreatedAt(),
				correction.getReadAt(),
				toItems(correction)
		);
	}

	@Transactional(readOnly = true)
	public Optional<PublicCorrectionResponse> findPublicCorrection(Long diaryId) {
		return correctionRepository.findCorrectionWithItemsByDiaryId(diaryId)
				.map(correction -> new PublicCorrectionResponse(
						toCorrector(correction),
						correction.getComment(),
						correction.getCreatedAt(),
						toItems(correction)));
	}

	@Transactional
	public void modifyCorrectionRead(Long userId, Long diaryId) {
		findCorrectionOfOwnedDiary(userId, diaryId).markRead(Instant.now());
	}

	@Transactional(readOnly = true)
	public CorrectionFindAllResponse findAllMyCorrection(Long userId, Long cursor, int size) {
		List<Correction> fetched = correctionRepository.findAllCorrectionByCorrectorId(userId, cursor, Limit.of(size + 1));
		boolean hasNext = fetched.size() > size;
		List<Correction> corrections = hasNext ? fetched.subList(0, size) : fetched;
		List<CorrectionFindAllResponse.Activity> activities = corrections.stream()
				.map(correction -> {
					Diary diary = correction.getRequest().getDiary();
					return new CorrectionFindAllResponse.Activity(
							correction.getId(),
							diary.getId(),
							diary.getDiaryDate(),
							excerpt(diary.getContent()),
							correction.getComment(),
							correction.getCreatedAt());
				})
				.toList();
		return new CorrectionFindAllResponse(activities, hasNext ? corrections.getLast().getId() : null);
	}

	@Transactional(readOnly = true)
	public boolean hasUnreadCorrection(Long userId) {
		return correctionRepository.existsUnreadCorrectionByDiaryUserId(userId);
	}

	@Transactional(readOnly = true)
	public Map<Long, CorrectionBadge> findAllCorrectionBadge(Collection<Long> diaryIds) {
		if (diaryIds.isEmpty()) {
			return Map.of();
		}
		Map<Long, CorrectionBadge> badges = new HashMap<>();
		diaryIds.forEach(diaryId -> badges.put(diaryId, CorrectionBadge.NOT_CORRECTED));
		correctionRepository.findAllCorrectionReadStateByDiaryIdIn(diaryIds).forEach(state -> badges.put(
				state.getDiaryId(),
				state.getReadAt() == null ? CorrectionBadge.UNREAD : CorrectionBadge.NONE));
		return badges;
	}

	@Transactional(readOnly = true)
	public boolean hasCorrected(Long userId, Long diaryId) {
		return correctionRepository.existsCorrectionByRequestDiaryIdAndCorrectorId(diaryId, userId);
	}

	@Transactional
	public void handleCorrectorWithdrawal(Long userId) {
		correctionRepository.findAllRequestIdByCorrectorId(userId)
				.forEach(requestId -> correctionRequestRepository.findById(requestId).ifPresent(CorrectionRequest::reopen));
		correctionRequestRepository.findAllCorrectionRequestByClaimedById(userId)
				.forEach(CorrectionRequest::release);
		correctionRequestRepository.flush();
	}

	private CorrectionRequest findRequest(Long diaryId) {
		return correctionRequestRepository.findCorrectionRequestByDiaryId(diaryId)
				.orElseThrow(() -> new CustomException(DiaryErrorCode.DIARY_NOT_FOUND));
	}

	private Correction findCorrectionOfOwnedDiary(Long userId, Long diaryId) {
		CorrectionRequest request = findRequest(diaryId);
		if (!request.getDiary().isWrittenBy(userId)) {
			throw new CustomException(DiaryErrorCode.DIARY_NOT_FOUND);
		}
		return correctionRepository.findCorrectionByRequestDiaryId(diaryId)
				.orElseThrow(() -> new CustomException(CorrectionErrorCode.CORRECTION_NOT_FOUND));
	}

	private void validateItems(Map<Long, DiarySentence> sentences, List<CorrectionSaveRequest.Item> items) {
		Set<Long> answered = new HashSet<>();
		for (CorrectionSaveRequest.Item item : items) {
			if (!sentences.containsKey(item.sentenceId()) || !answered.add(item.sentenceId())) {
				throw new CustomException(CorrectionErrorCode.INCOMPLETE_ITEMS);
			}
			if (item.changed() && (item.correctedContent() == null || item.correctedContent().isBlank())) {
				throw new CustomException(CorrectionErrorCode.INVALID_ITEM);
			}
		}
		if (answered.size() != sentences.size()) {
			throw new CustomException(CorrectionErrorCode.INCOMPLETE_ITEMS);
		}
	}

	private CorrectionFindResponse.Corrector toCorrector(Correction correction) {
		Long correctorId = correction.getCorrector().getId();
		Profile corrector = profileService.findAllProfileById(List.of(correctorId)).get(correctorId);
		return new CorrectionFindResponse.Corrector(
				correctorId,
				corrector == null ? null : corrector.getNickname(),
				corrector == null ? null : profileService.profileImageUrl(corrector));
	}

	private List<CorrectionFindResponse.Item> toItems(Correction correction) {
		return correction.getItems().stream()
				.sorted(Comparator.comparingInt(item -> item.getSentence().getSentenceIndex()))
				.map(this::toItem)
				.toList();
	}

	private CorrectionFindResponse.Item toItem(CorrectionItem item) {
		DiarySentence sentence = item.getSentence();
		return new CorrectionFindResponse.Item(
				sentence.getId(),
				sentence.getSentenceIndex(),
				sentence.getContent(),
				item.isChanged(),
				item.getCorrectedContent(),
				item.getReason()
		);
	}

	private DiaryAuthorResponse toAuthor(Long authorId) {
		Profile profile = profileService.findAllProfileById(List.of(authorId)).get(authorId);
		if (profile == null) {
			return null;
		}
		return new DiaryAuthorResponse(
				profile.getUserId(),
				profile.getNickname(),
				profileService.profileImageUrl(profile),
				profile.getNationality(),
				profile.getLearningLanguage(),
				profile.getLearningLevel()
		);
	}

	private String excerpt(String content) {
		return content.length() <= EXCERPT_LENGTH ? content : content.substring(0, EXCERPT_LENGTH);
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
