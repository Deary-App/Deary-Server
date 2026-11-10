package com.deary.server.domain.diary.service;

import com.deary.server.domain.correction.entity.CorrectionProgress;
import com.deary.server.domain.correction.entity.CorrectionRequest;
import com.deary.server.domain.correction.repository.CorrectionRequestRepository;
import com.deary.server.domain.diary.dto.response.DiaryAuthorResponse;
import com.deary.server.domain.diary.dto.response.DiaryPhotoResponse;
import com.deary.server.domain.diary.dto.response.PublicDiaryFindAllResponse;
import com.deary.server.domain.diary.dto.response.PublicDiaryFindResponse;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.entity.DiaryPhoto;
import com.deary.server.domain.diary.exception.DiaryErrorCode;
import com.deary.server.domain.diary.repository.DiaryPhotoRepository;
import com.deary.server.domain.diary.repository.DiaryRepository;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicDiaryService {

	private final DiaryRepository diaryRepository;
	private final DiaryPhotoRepository diaryPhotoRepository;
	private final CorrectionRequestRepository correctionRequestRepository;
	private final ProfileService profileService;
	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;

	@Transactional(readOnly = true)
	public PublicDiaryFindAllResponse findAllPublicDiary(Language language, Long cursor, int size) {
		List<Diary> diaries = diaryRepository.findAllPublicDiary(language, cursor, Limit.of(size + 1));
		return toPage(diaries, size);
	}

	@Transactional(readOnly = true)
	public PublicDiaryFindAllResponse findAllPublicDiaryByUser(Long authorId, Long cursor, int size) {
		profileService.findProfile(authorId);
		List<Diary> diaries = diaryRepository.findAllPublicDiaryByUserId(authorId, cursor, Limit.of(size + 1));
		return toPage(diaries, size);
	}

	@Transactional(readOnly = true)
	public PublicDiaryFindResponse findPublicDiary(Long userId, Long diaryId) {
		Diary diary = diaryRepository.findById(diaryId)
				.orElseThrow(() -> new CustomException(DiaryErrorCode.DIARY_NOT_FOUND));
		Long authorId = diary.getUser().getId();
		Profile author = profileService.findAllProfileById(List.of(authorId)).get(authorId);
		Map<String, String> urls = signedUrls(diary.getPhotos().stream().map(DiaryPhoto::getStoragePath).toList());
		List<DiaryPhotoResponse> photos = diary.getPhotos().stream()
				.map(photo -> DiaryPhotoResponse.of(photo, urls.get(photo.getStoragePath())))
				.toList();
		Instant now = Instant.now();
		Optional<CorrectionRequest> request = correctionRequestRepository.findCorrectionRequestByDiaryId(diaryId);
		boolean mine = diary.isWrittenBy(userId);
		return new PublicDiaryFindResponse(
				diary.getId(),
				diary.getDiaryDate(),
				diary.getTitle(),
				diary.getContent(),
				diary.getLanguage(),
				photos,
				toAuthor(author),
				request.map(r -> r.progressAt(now)).orElse(CorrectionProgress.WAITING),
				mine,
				!mine && request.map(r -> r.isCorrectableBy(userId, now)).orElse(false)
		);
	}

	private PublicDiaryFindAllResponse toPage(List<Diary> fetched, int size) {
		boolean hasNext = fetched.size() > size;
		List<Diary> diaries = hasNext ? fetched.subList(0, size) : fetched;
		if (diaries.isEmpty()) {
			return new PublicDiaryFindAllResponse(List.of(), null);
		}
		List<Long> diaryIds = diaries.stream().map(Diary::getId).toList();
		Map<Long, Profile> profiles = profileService.findAllProfileById(
				diaries.stream().map(diary -> diary.getUser().getId()).distinct().toList());
		Map<Long, String> firstPhotoPaths = diaryPhotoRepository.findAllDiaryPhotoByDiaryIdInAndSortOrder(diaryIds, 0)
				.stream()
				.collect(Collectors.toMap(photo -> photo.getDiary().getId(), DiaryPhoto::getStoragePath));
		Map<Long, CorrectionRequest> requests = correctionRequestRepository.findAllCorrectionRequestByDiaryIdIn(diaryIds)
				.stream()
				.collect(Collectors.toMap(request -> request.getDiary().getId(), Function.identity()));
		Map<String, String> urls = signedUrls(List.copyOf(firstPhotoPaths.values()));
		Instant now = Instant.now();
		List<PublicDiaryFindAllResponse.PublicDiaryCard> cards = diaries.stream()
				.map(diary -> new PublicDiaryFindAllResponse.PublicDiaryCard(
						diary.getId(),
						diary.getDiaryDate(),
						diary.getTitle(),
						diary.getLanguage(),
						urls.get(firstPhotoPaths.get(diary.getId())),
						toAuthor(profiles.get(diary.getUser().getId())),
						requests.containsKey(diary.getId())
								? requests.get(diary.getId()).progressAt(now)
								: CorrectionProgress.WAITING))
				.toList();
		return new PublicDiaryFindAllResponse(cards, hasNext ? diaries.getLast().getId() : null);
	}

	private DiaryAuthorResponse toAuthor(Profile profile) {
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

	private Map<String, String> signedUrls(List<String> paths) {
		return storageClient.createDownloadUrls(storageProperties.diaryBucket(), paths);
	}

}
