package com.deary.server.domain.report.service;

import com.deary.server.domain.correction.entity.Correction;
import com.deary.server.domain.correction.entity.CorrectionItem;
import com.deary.server.domain.correction.repository.CorrectionRepository;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.entity.DiaryPhoto;
import com.deary.server.domain.diary.repository.DiaryRepository;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.report.dto.request.ReportModifyRequest;
import com.deary.server.domain.report.dto.response.ReportFindAllResponse;
import com.deary.server.domain.report.dto.response.ReportFindResponse;
import com.deary.server.domain.report.entity.Report;
import com.deary.server.domain.report.entity.ReportReason;
import com.deary.server.domain.report.entity.ReportSource;
import com.deary.server.domain.report.entity.ReportStatus;
import com.deary.server.domain.report.entity.ReportTargetType;
import com.deary.server.domain.report.event.ReportRegisteredEvent;
import com.deary.server.domain.report.exception.ReportErrorCode;
import com.deary.server.domain.report.repository.ReportRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 운영자 신고 처리. 처리 결과는 같은 대상에 들어온 신고 전체에 함께 기록한다.
 */
@Service
@RequiredArgsConstructor
public class ReportModerationService {

	private static final int LIST_LIMIT = 300;

	private final ReportRepository reportRepository;
	private final DiaryRepository diaryRepository;
	private final CorrectionRepository correctionRepository;
	private final ProfileService profileService;
	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public ReportFindAllResponse findAllReport(ReportStatus status) {
		List<Report> reports = reportRepository.findAllReportByStatusOrderByIdDesc(status, Limit.of(LIST_LIMIT));
		Map<String, List<Report>> groups = new LinkedHashMap<>();
		for (Report report : reports) {
			groups.computeIfAbsent(report.getTargetType() + ":" + report.getTargetId(), key -> new ArrayList<>())
					.add(report);
		}
		List<ReportFindAllResponse.ReportGroup> items = groups.values().stream()
				.map(group -> {
					Report latest = group.getFirst();
					List<ReportReason> reasons = group.stream().map(Report::getReason).distinct().toList();
					return new ReportFindAllResponse.ReportGroup(latest.getId(), latest.getTargetType(),
							latest.getTargetId(), reasons, group.size(), group.stream().anyMatch(Report::isAuto),
							latest.getCreatedAt(), latest.getStatus());
				})
				.toList();
		return new ReportFindAllResponse(items);
	}

	@Transactional(readOnly = true)
	public ReportFindResponse findReport(Long reportId) {
		Report report = findReportEntity(reportId);
		List<Report> sameTarget = findAllSameTarget(report);
		return new ReportFindResponse(
				report.getId(),
				report.getTargetType(),
				report.getTargetId(),
				report.getStatus(),
				report.getMemo(),
				report.getHandledAt(),
				sameTarget.stream()
						.sorted(Comparator.comparing(Report::getId).reversed())
						.map(entry -> new ReportFindResponse.Entry(entry.getId(), entry.getSource(), entry.getReason(), entry.getDetail(),
								entry.getCreatedAt()))
						.toList(),
				toTargetUser(report.getTargetUser()),
				report.getTargetType() == ReportTargetType.DIARY
						? diaryRepository.findById(report.getTargetId()).map(this::toDiaryTarget).orElse(null)
						: null,
				report.getTargetType() == ReportTargetType.CORRECTION
						? correctionRepository.findById(report.getTargetId()).map(this::toCorrectionTarget).orElse(null)
						: null
		);
	}

	@Transactional
	public ReportFindResponse modifyReport(Long reportId, ReportModifyRequest request) {
		Report report = findReportEntity(reportId);
		Instant now = Instant.now();
		ReportStatus nextStatus = switch (request.action()) {
			case DISMISS -> {
				// 문제없음이면 자동 숨김을 포함해 걸려 있던 숨김·정지를 푼다
				revert(report);
				yield ReportStatus.DISMISSED;
			}
			case HIDE -> {
				hideTarget(report, now);
				yield ReportStatus.HIDDEN;
			}
			case SUSPEND -> {
				findTargetUser(report).suspend(now);
				yield ReportStatus.SUSPENDED;
			}
			case REOPEN -> {
				revert(report);
				yield ReportStatus.PENDING;
			}
		};
		String memo = blankToNull(request.memo());
		findAllSameTarget(report).forEach(entry -> entry.handle(nextStatus, memo, now));
		reportRepository.flush();
		return findReport(reportId);
	}

	private void hideTarget(Report report, Instant now) {
		switch (report.getTargetType()) {
			case DIARY -> findDiary(report).hide(now);
			case CORRECTION -> findCorrection(report).hide(now);
			case USER -> profileService.findProfile(findTargetUser(report).getId()).hideProfileImage(now);
		}
	}

	// 지금 상태에 맞춰 숨김·정지를 푼다. 대상이 이미 삭제됐으면 상태만 대기로 돌린다.
	private void revert(Report report) {
		if (report.getStatus() == ReportStatus.SUSPENDED) {
			findTargetUser(report).unsuspend();
			return;
		}
		if (report.getStatus() != ReportStatus.HIDDEN && report.getStatus() != ReportStatus.AUTO_HIDDEN) {
			return;
		}
		switch (report.getTargetType()) {
			case DIARY -> diaryRepository.findById(report.getTargetId()).ifPresent(Diary::unhide);
			case CORRECTION -> correctionRepository.findById(report.getTargetId()).ifPresent(Correction::unhide);
			case USER -> Optional.ofNullable(report.getTargetUser())
					.map(user -> profileService.findAllProfileById(List.of(user.getId())).get(user.getId()))
					.ifPresent(Profile::unhideProfileImage);
		}
	}

	/**
	 * AI 자동 검수에 보낼 글과 사진 주소. 대상이 이미 삭제됐거나 숨겨져 있으면 비어 있다.
	 */
	@Transactional(readOnly = true)
	public Optional<AutoModerationTarget> findAutoModerationTarget(ReportTargetType targetType, Long targetId) {
		return switch (targetType) {
			case DIARY -> diaryRepository.findById(targetId)
					.filter(diary -> !diary.isHidden())
					.map(diary -> {
						List<String> paths = diary.getPhotos().stream().map(DiaryPhoto::getStoragePath).toList();
						Map<String, String> urls = storageClient.createDownloadUrls(storageProperties.diaryBucket(), paths);
						return new AutoModerationTarget(diary.getTitle() + "\n\n" + diary.getContent(),
								paths.stream().map(urls::get).filter(url -> url != null).toList());
					});
			case CORRECTION -> correctionRepository.findById(targetId)
					.filter(correction -> !correction.isHidden())
					.map(correction -> {
						StringBuilder text = new StringBuilder(correction.getComment());
						correction.getItems().stream()
								.filter(CorrectionItem::isChanged)
								.forEach(item -> text.append("\n").append(item.getCorrectedContent())
										.append(item.getReason() == null ? "" : "\n" + item.getReason()));
						return new AutoModerationTarget(text.toString(), List.of());
					});
			case USER -> Optional.ofNullable(profileService.findAllProfileById(List.of(targetId)).get(targetId))
					.filter(profile -> !profile.isProfileImageHidden())
					.map(profile -> storageClient.publicUrl(storageProperties.profileBucket(),
							profile.getProfileImagePath()))
					.map(url -> new AutoModerationTarget(null, List.of(url)));
		};
	}

	/**
	 * AI 자동 검수에 걸린 대상을 숨기고, 운영자가 확인하도록 신고함에 올린다.
	 * 대상마다 자동 신고는 하나만 두고, 다시 걸리면 그 신고를 다시 확인 대기로 돌린다.
	 */
	@Transactional
	public void registerAutoReport(ReportTargetType targetType, Long targetId, ReportReason reason, String detail) {
		Instant now = Instant.now();
		Optional<User> targetUser = switch (targetType) {
			case DIARY -> diaryRepository.findById(targetId).map(diary -> {
				diary.hide(now);
				return diary.getUser();
			});
			case CORRECTION -> correctionRepository.findById(targetId).map(correction -> {
				correction.hide(now);
				return correction.getCorrector();
			});
			case USER -> Optional.ofNullable(profileService.findAllProfileById(List.of(targetId)).get(targetId))
					.map(profile -> {
						profile.hideProfileImage(now);
						return profile.getUser();
					});
		};
		if (targetUser.isEmpty()) {
			return;
		}
		Report report = reportRepository.findReportBySourceAndTargetTypeAndTargetId(ReportSource.AUTO, targetType,
						targetId)
				.map(existing -> {
					existing.reflag(reason, detail);
					return existing;
				})
				.orElseGet(() -> reportRepository.save(
						Report.createAuto(targetType, targetId, targetUser.get(), reason, detail)));
		reportRepository.flush();
		eventPublisher.publishEvent(new ReportRegisteredEvent(report.getId(), targetType, targetId, reason,
				ReportSource.AUTO, report.getUpdatedAt()));
	}

	private Report findReportEntity(Long reportId) {
		return reportRepository.findById(reportId)
				.orElseThrow(() -> new CustomException(ReportErrorCode.REPORT_NOT_FOUND));
	}

	private List<Report> findAllSameTarget(Report report) {
		return reportRepository.findAllReportByTargetTypeAndTargetIdOrderByIdAsc(report.getTargetType(),
				report.getTargetId());
	}

	private User findTargetUser(Report report) {
		if (report.getTargetUser() == null) {
			throw new CustomException(ReportErrorCode.TARGET_GONE);
		}
		return report.getTargetUser();
	}

	private Diary findDiary(Report report) {
		return diaryRepository.findById(report.getTargetId())
				.orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_GONE));
	}

	private Correction findCorrection(Report report) {
		return correctionRepository.findById(report.getTargetId())
				.orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_GONE));
	}

	private ReportFindResponse.TargetUser toTargetUser(User user) {
		if (user == null) {
			return null;
		}
		Profile profile = profileService.findAllProfileById(List.of(user.getId())).get(user.getId());
		return new ReportFindResponse.TargetUser(
				user.getId(),
				profile == null ? null : profile.getNickname(),
				// 운영자는 내린 사진도 확인할 수 있게 원본 주소를 준다
				profile == null ? null : storageClient.publicUrl(storageProperties.profileBucket(),
						profile.getProfileImagePath()),
				profile != null && profile.isProfileImageHidden(),
				user.isSuspended(),
				reportRepository.countReportByTargetUserId(user.getId())
		);
	}

	private ReportFindResponse.DiaryTarget toDiaryTarget(Diary diary) {
		List<String> paths = diary.getPhotos().stream().map(DiaryPhoto::getStoragePath).toList();
		Map<String, String> urls = storageClient.createDownloadUrls(storageProperties.diaryBucket(), paths);
		return new ReportFindResponse.DiaryTarget(
				diary.getId(),
				diary.getDiaryDate(),
				diary.getTitle(),
				diary.getContent(),
				paths.stream().map(urls::get).filter(url -> url != null).toList(),
				diary.isHidden()
		);
	}

	private ReportFindResponse.CorrectionTarget toCorrectionTarget(Correction correction) {
		List<ReportFindResponse.Item> items = correction.getItems().stream()
				.filter(CorrectionItem::isChanged)
				.sorted(Comparator.comparingInt(item -> item.getSentence().getSentenceIndex()))
				.map(item -> new ReportFindResponse.Item(item.getSentence().getContent(), item.getCorrectedContent(),
						item.getReason()))
				.toList();
		return new ReportFindResponse.CorrectionTarget(
				correction.getId(),
				correction.getRequest().getDiary().getId(),
				correction.getComment(),
				items,
				correction.isHidden()
		);
	}

	public record AutoModerationTarget(
			String text,
			List<String> imageUrls
	) {
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
