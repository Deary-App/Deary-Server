package com.deary.server.domain.report.service;

import com.deary.server.domain.correction.event.CorrectionCompletedEvent;
import com.deary.server.domain.diary.event.DiaryCreatedEvent;
import com.deary.server.domain.profile.event.ProfileImageChangedEvent;
import com.deary.server.domain.report.entity.ReportTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 일기·교정·프로필 사진이 저장된 뒤 따로 자동 검수를 돌린다. 검사가 느리거나 실패해도 원래 요청에는 영향이 없다.
 */
@Component
@RequiredArgsConstructor
public class AutoModerationListener {

	private final AutoModerationService autoModerationService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void moderateDiary(DiaryCreatedEvent event) {
		autoModerationService.moderate(ReportTargetType.DIARY, event.diaryId());
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void moderateCorrection(CorrectionCompletedEvent event) {
		autoModerationService.moderate(ReportTargetType.CORRECTION, event.correctionId());
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void moderateProfileImage(ProfileImageChangedEvent event) {
		autoModerationService.moderate(ReportTargetType.USER, event.userId());
	}

}
