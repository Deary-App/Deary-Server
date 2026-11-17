package com.deary.server.domain.push.service;

import com.deary.server.domain.push.client.ExpoPushClient;
import com.deary.server.domain.push.client.ExpoPushClient.ReceiptResult;
import com.deary.server.domain.push.entity.PushTicket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 15분마다 Expo 전달 결과(receipt)를 조회해 앱이 지워진 기기의 푸시 토큰을 정리한다.
 * Expo는 보낸 지 15분쯤 지나 결과를 조회하라고 권하고, 결과는 24시간만 보관한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushReceiptScheduler {

	private static final Duration RECEIPT_DELAY = Duration.ofMinutes(15);
	private static final Duration RECEIPT_RETENTION = Duration.ofHours(24);
	// Expo receipt 조회 한 번에 넣을 수 있는 최대 접수표 수
	static final int MAX_TICKETS_PER_REQUEST = 1000;

	private final PushTicketService pushTicketService;
	private final ExpoPushClient expoPushClient;

	@Scheduled(initialDelay = 15, fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
	public void checkReceipts() {
		handleReceiptCheck(Instant.now());
	}

	public void handleReceiptCheck(Instant now) {
		if (!expoPushClient.enabled()) {
			return;
		}
		Instant createdBefore = now.minus(RECEIPT_DELAY);
		long lastId = 0L;
		int checked = 0;
		int deviceNotRegistered = 0;
		try {
			// 결과 조회가 실패해도 보관 기간이 지난 접수표는 먼저 정리한다(따로 커밋된다).
			int stale = pushTicketService.removeAllStalePushTicket(now.minus(RECEIPT_RETENTION));
			List<PushTicket> tickets = pushTicketService.findAllPushTicketCreatedBefore(
					createdBefore, lastId, MAX_TICKETS_PER_REQUEST);
			// 결과가 아직 없는 접수표가 1000건을 넘어도 뒤의 접수표까지 한 번씩 조회한다.
			while (!tickets.isEmpty()) {
				ReceiptResult result = expoPushClient.findAllReceipt(tickets.stream().map(PushTicket::getTicketId).toList());
				pushTicketService.handleReceiptResult(tickets, result);
				checked += result.checkedTicketIds().size();
				deviceNotRegistered += result.deviceNotRegisteredTicketIds().size();
				lastId = tickets.getLast().getId();
				tickets = pushTicketService.findAllPushTicketCreatedBefore(createdBefore, lastId, MAX_TICKETS_PER_REQUEST);
			}
			if (stale > 0 || checked > 0) {
				log.info("푸시 전달 결과 확인 checked={} deviceNotRegistered={} stale={}", checked, deviceNotRegistered, stale);
			}
		} catch (RuntimeException exception) {
			log.warn("푸시 전달 결과 확인 실패 error={}", exception.getClass().getSimpleName());
		}
	}

}
