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

	/**
	 * createdBefore 이전에 만든 접수표를 lastId 다음부터 ID 순서로 size건 조회한다.
	 */
	@Transactional(readOnly = true)
	public List<PushTicket> findAllPushTicketCreatedBefore(Instant createdBefore, long lastId, int size) {
		return pushTicketRepository.findAllPushTicketByCreatedAtBeforeAndIdGreaterThan(createdBefore, lastId, Limit.of(size));
	}

	/**
	 * Expo가 결과를 더 이상 보관하지 않는(staleBefore 이전) 접수표를 지운다. 결과 조회 성공과 상관없이 정리한다.
	 */
	@Transactional
	public int removeAllStalePushTicket(Instant staleBefore) {
		return pushTicketRepository.deleteAllPushTicketByCreatedAtBefore(staleBefore);
	}

	/**
	 * '등록되지 않은 기기'로 확인된 토큰을 지우고, 결과가 나온 접수표를 지운다.
	 * 푸시를 보낸 뒤 같은 토큰이 다시 등록됐으면(updatedAt이 접수 시각 이후) 그 토큰은 남긴다.
	 */
	@Transactional
	public void handleReceiptResult(List<PushTicket> tickets, ReceiptResult result) {
		tickets.stream()
				.filter(ticket -> result.deviceNotRegisteredTicketIds().contains(ticket.getTicketId()))
				.forEach(ticket -> pushTokenRepository.deletePushTokenByTokenAndUpdatedAtBefore(
						ticket.getToken(), ticket.getCreatedAt()));
		List<Long> checkedIds = tickets.stream()
				.filter(ticket -> result.checkedTicketIds().contains(ticket.getTicketId()))
				.map(PushTicket::getId)
				.toList();
		if (!checkedIds.isEmpty()) {
			pushTicketRepository.deleteAllPushTicketByIdIn(checkedIds);
		}
	}

}
