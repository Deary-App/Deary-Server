package com.deary.server.domain.report.service;

import com.deary.server.domain.report.client.ReportWebhookClient;
import com.deary.server.domain.report.event.ReportRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportNotificationListener {

	private final ReportWebhookClient reportWebhookClient;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void notifyReportRegistered(ReportRegisteredEvent event) {
		if (!reportWebhookClient.configured()) {
			return;
		}
		try {
			reportWebhookClient.sendReportRegistered(event);
			log.info("신고 알림 전송 완료 reportId={}", event.reportId());
		} catch (RuntimeException exception) {
			log.warn("신고 알림 전송 실패 reportId={} error={}", event.reportId(), exception.getClass().getSimpleName());
		}
	}

}
