package com.deary.server.domain.report.service;

import com.deary.server.domain.correction.entity.Correction;
import com.deary.server.domain.correction.repository.CorrectionRepository;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.repository.DiaryRepository;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.report.dto.request.ReportSaveRequest;
import com.deary.server.domain.report.dto.response.ReportCreateResponse;
import com.deary.server.domain.report.entity.Report;
import com.deary.server.domain.report.entity.ReportTargetType;
import com.deary.server.domain.report.exception.ReportErrorCode;
import com.deary.server.domain.report.repository.ReportRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CommonErrorCode;
import com.deary.server.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final DiaryRepository diaryRepository;
	private final CorrectionRepository correctionRepository;
	private final UserService userService;
	private final ProfileService profileService;

	@Transactional
	public ReportCreateResponse registerReport(Long userId, ReportSaveRequest request) {
		User reporter = userService.findUser(userId);
		User targetUser = findTargetUser(request.targetType(), request.targetId())
				.orElseThrow(() -> new CustomException(ReportErrorCode.TARGET_NOT_FOUND));
		if (targetUser.getId().equals(userId)) {
			throw new CustomException(ReportErrorCode.SELF_REPORT);
		}
		Optional<Report> existingReport = reportRepository.findReportByReporterIdAndTargetTypeAndTargetId(
				userId, request.targetType(), request.targetId());
		if (existingReport.isPresent()) {
			return new ReportCreateResponse(existingReport.get().getId());
		}
		Report report = Report.create(reporter, request.targetType(), request.targetId(), targetUser,
				request.reason(), blankToNull(request.detail()));
		try {
			reportRepository.saveAndFlush(report);
		} catch (DataIntegrityViolationException exception) {
			throw new CustomException(CommonErrorCode.CONFLICT);
		}
		return new ReportCreateResponse(report.getId());
	}

	@Transactional(readOnly = true)
	public boolean hasReported(Long userId, ReportTargetType targetType, Long targetId) {
		return reportRepository.existsReportByReporterIdAndTargetTypeAndTargetId(userId, targetType, targetId);
	}

	private Optional<User> findTargetUser(ReportTargetType targetType, Long targetId) {
		return switch (targetType) {
			case DIARY -> diaryRepository.findById(targetId).map(Diary::getUser);
			case CORRECTION -> correctionRepository.findById(targetId).map(Correction::getCorrector);
			case USER -> Optional.ofNullable(profileService.findAllProfileById(List.of(targetId)).get(targetId))
					.map(Profile::getUser);
		};
	}

	private String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
