package com.deary.server.domain.push.service;

import com.deary.server.domain.push.client.ExpoPushClient.AcceptedTicket;
import com.deary.server.domain.push.client.ExpoPushClient.ReceiptResult;
import com.deary.server.domain.push.entity.PushTicket;
import com.deary.server.domain.push.repository.PushTicketRepository;
import com.deary.server.domain.push.repository.PushTokenRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Expo 접수표를 저장하고, 전달 결과(receipt)를 보고 앱이 지워진 기기의 토큰을 정리한다.
 */
@Service
@RequiredArgsConstructor
public class PushTicketService {

	private final PushTicketRepository pushTicketRepository;
	private final PushTokenRepository pushTokenRepository;

	@Transactional
	public void registerAllPushTicket(List<AcceptedTicket> tickets) {
		if (!tickets.isEmpty()) {
			pushTicketRepository.saveAll(tickets.stream()
					.map(ticket -> PushTicket.create(ticket.ticketId(), ticket.token()))
					.toList());
		}
	}

	@Transactional(readOnly = true)
	public List<PushTicket> findAllPushTicketCreatedBefore(Instant createdBefore, int size) {
		return pushTicketRepository.findAllPushTicketByCreatedAtBefore(createdBefore, Limit.of(size));
	}

	/**
	 * '등록되지 않은 기기'로 확인된 토큰을 지우고, 결과가 나왔거나 결과를 더 기다릴 수 없는(staleBefore 이전) 접수표를 지운다.
	 */
	@Transactional
	public void handleReceiptResult(List<PushTicket> tickets, ReceiptResult result, Instant staleBefore) {
		List<String> expiredTokens = tickets.stream()
				.filter(ticket -> result.deviceNotRegisteredTicketIds().contains(ticket.getTicketId()))
				.map(PushTicket::getToken)
				.distinct()
				.toList();
		if (!expiredTokens.isEmpty()) {
			pushTokenRepository.deleteAllPushTokenByTokenIn(expiredTokens);
		}
		List<Long> doneTicketIds = tickets.stream()
				.filter(ticket -> result.checkedTicketIds().contains(ticket.getTicketId())
						|| ticket.getCreatedAt().isBefore(staleBefore))
				.map(PushTicket::getId)
				.toList();
		if (!doneTicketIds.isEmpty()) {
			pushTicketRepository.deleteAllPushTicketByIdIn(doneTicketIds);
		}
	}

}
