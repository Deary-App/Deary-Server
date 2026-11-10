package com.deary.server.domain.diary.service;

import com.deary.server.domain.correction.entity.CorrectionProgress;
import com.deary.server.domain.correction.entity.CorrectionRequest;
import com.deary.server.domain.correction.repository.CorrectionRequestRepository;
import com.deary.server.domain.diary.dto.request.DiarySaveRequest;
import com.deary.server.domain.diary.dto.response.DiaryCreateResponse;
import com.deary.server.domain.diary.dto.response.DiaryFindAllByMonthResponse;
import com.deary.server.domain.diary.dto.response.DiaryFindResponse;
import com.deary.server.domain.diary.dto.response.DiaryPhotoResponse;
import com.deary.server.domain.diary.dto.response.DiaryTodayFindResponse;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.entity.DiaryPhoto;
import com.deary.server.domain.diary.exception.DiaryErrorCode;
import com.deary.server.domain.diary.repository.DiaryPhotoRepository;
import com.deary.server.domain.diary.repository.DiaryRepository;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.upload.service.StoragePathPolicy;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryService {

	private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

	private final DiaryRepository diaryRepository;
	private final DiaryPhotoRepository diaryPhotoRepository;
	private final CorrectionRequestRepository correctionRequestRepository;
	private final UserService userService;
	private final ProfileService profileService;
	private final SentenceSplitter sentenceSplitter;
	private final StoragePathPolicy storagePathPolicy;
	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;

	@Transactional
	public DiaryCreateResponse generateDiary(Long userId, DiarySaveRequest request) {
		User user = userService.findUser(userId);
		Profile profile = profileService.findProfile(userId);
		LocalDate today = LocalDate.now(SERVICE_ZONE);
		if (diaryRepository.findDiaryByUserIdAndDiaryDate(userId, today).isPresent()) {
			throw new CustomException(DiaryErrorCode.ALREADY_WRITTEN_TODAY);
		}
		validatePhotos(userId, request.photos());
		List<String> sentences = sentenceSplitter.split(request.content());
		if (sentences.isEmpty()) {
			throw new CustomException(DiaryErrorCode.EMPTY_CONTENT);
		}
		Diary diary = Diary.create(user, today, request.title().strip(), request.content().strip(),
				profile.getLearningLanguage());
		request.photos().forEach(photo -> diary.addPhoto(photo.path(), new DiaryPhoto.Layout(
				photo.positionX(), photo.positionY(), photo.scale(), photo.rotation())));
		sentences.forEach(diary::addSentence);
		try {
			diaryRepository.saveAndFlush(diary);
			correctionRequestRepository.saveAndFlush(CorrectionRequest.open(diary));
		} catch (DataIntegrityViolationException exception) {
			throw new CustomException(DiaryErrorCode.ALREADY_WRITTEN_TODAY);
		}
		String firstPhotoPath = diary.getPhotos().getFirst().getStoragePath();
		return new DiaryCreateResponse(diary.getId(), signedUrls(List.of(firstPhotoPath)).get(firstPhotoPath));
	}

	@Transactional(readOnly = true)
	public DiaryTodayFindResponse findTodayDiary(Long userId) {
		LocalDate today = LocalDate.now(SERVICE_ZONE);
		return diaryRepository.findDiaryByUserIdAndDiaryDate(userId, today)
				.map(diary -> new DiaryTodayFindResponse(today, true, diary.getId()))
				.orElseGet(() -> new DiaryTodayFindResponse(today, false, null));
	}

	@Transactional(readOnly = true)
	public DiaryFindResponse findMyDiary(Long userId, Long diaryId) {
		Diary diary = findOwnedDiary(userId, diaryId);
		Map<String, String> urls = signedUrls(diary.getPhotos().stream().map(DiaryPhoto::getStoragePath).toList());
		List<DiaryPhotoResponse> photos = diary.getPhotos().stream()
				.map(photo -> DiaryPhotoResponse.of(photo, urls.get(photo.getStoragePath())))
				.toList();
		return new DiaryFindResponse(
				diary.getId(),
				diary.getDiaryDate(),
				diary.getTitle(),
				diary.getContent(),
				diary.getLanguage(),
				photos,
				progressOf(diary.getId())
		);
	}

	@Transactional
	public void removeDiary(Long userId, Long diaryId) {
		Diary diary = findOwnedDiary(userId, diaryId);
		List<String> paths = diary.getPhotos().stream().map(DiaryPhoto::getStoragePath).toList();
		diaryRepository.delete(diary);
		diaryRepository.flush();
		storageClient.removeFiles(storageProperties.diaryBucket(), paths);
	}

	@Transactional(readOnly = true)
	public DiaryFindAllByMonthResponse findAllDiaryByMonth(Long userId, int year, int month) {
		YearMonth yearMonth = toYearMonth(year, month);
		List<Diary> diaries = diaryRepository.findAllDiaryByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(
				userId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
		List<Long> diaryIds = diaries.stream().map(Diary::getId).toList();
		Map<Long, String> firstPhotoPaths = diaryIds.isEmpty() ? Map.of()
				: diaryPhotoRepository.findAllDiaryPhotoByDiaryIdInAndSortOrder(diaryIds, 0).stream()
						.collect(Collectors.toMap(photo -> photo.getDiary().getId(), DiaryPhoto::getStoragePath));
		Map<Long, CorrectionRequest> requests = diaryIds.isEmpty() ? Map.of()
				: correctionRequestRepository.findAllCorrectionRequestByDiaryIdIn(diaryIds).stream()
						.collect(Collectors.toMap(request -> request.getDiary().getId(), Function.identity()));
		Map<String, String> urls = signedUrls(List.copyOf(firstPhotoPaths.values()));
		Instant now = Instant.now();
		List<DiaryFindAllByMonthResponse.DiarySummary> summaries = diaries.stream()
				.map(diary -> new DiaryFindAllByMonthResponse.DiarySummary(
						diary.getId(),
						diary.getDiaryDate(),
						diary.getTitle(),
						urls.get(firstPhotoPaths.get(diary.getId())),
						requests.containsKey(diary.getId())
								? requests.get(diary.getId()).progressAt(now)
								: CorrectionProgress.WAITING))
				.toList();
		return new DiaryFindAllByMonthResponse(year, month, summaries);
	}

	private Diary findOwnedDiary(Long userId, Long diaryId) {
		return diaryRepository.findById(diaryId)
				.filter(diary -> diary.isWrittenBy(userId))
				.orElseThrow(() -> new CustomException(DiaryErrorCode.DIARY_NOT_FOUND));
	}

	private CorrectionProgress progressOf(Long diaryId) {
		return correctionRequestRepository.findCorrectionRequestByDiaryId(diaryId)
				.map(request -> request.progressAt(Instant.now()))
				.orElse(CorrectionProgress.WAITING);
	}

	private void validatePhotos(Long userId, List<DiarySaveRequest.Photo> photos) {
		Set<String> paths = new HashSet<>();
		for (DiarySaveRequest.Photo photo : photos) {
			if (!storagePathPolicy.isOwnedDiaryImagePath(userId, photo.path()) || !paths.add(photo.path())) {
				throw new CustomException(DiaryErrorCode.INVALID_PHOTO);
			}
		}
	}

	private Map<String, String> signedUrls(List<String> paths) {
		return storageClient.createDownloadUrls(storageProperties.diaryBucket(), paths);
	}

	private YearMonth toYearMonth(int year, int month) {
		try {
			return YearMonth.of(year, month);
		} catch (DateTimeException exception) {
			throw new CustomException(DiaryErrorCode.INVALID_MONTH);
		}
	}

}
