package com.deary.server.domain.push.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Expo 푸시 서비스(https://exp.host)로 알림을 보내고 전달 결과(receipt)를 조회한다. Expo가 APNs로 전달한다.
 */
@Component
@EnableConfigurationProperties(ExpoPushProperties.class)
public class ExpoPushClient {

	private static final String SEND_URL = "https://exp.host/--/api/v2/push/send";
	private static final String RECEIPTS_URL = "https://exp.host/--/api/v2/push/getReceipts";
	private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";
	private static final String STATUS_OK = "ok";
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

	private final ExpoPushProperties properties;
	private final RestClient restClient;

	public ExpoPushClient(ExpoPushProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		RestClient.Builder builder = RestClient.builder()
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		if (properties.hasAccessToken()) {
			builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.accessToken());
		}
		this.restClient = builder.build();
	}

	public boolean enabled() {
		return properties.enabled();
	}

	/**
	 * 메시지를 보내고, Expo가 '등록되지 않은 기기'라고 알려 준 토큰을 돌려준다(지워야 할 토큰).
	 */
	public SendResult send(List<ExpoPushMessage> messages) {
		if (messages.isEmpty()) {
			return new SendResult(List.of(), List.of());
		}
		SendResponse response = restClient.post()
				.uri(SEND_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.body(messages)
				.retrieve()
				.body(SendResponse.class);
		List<String> expiredTokens = new ArrayList<>();
		List<AcceptedTicket> acceptedTickets = new ArrayList<>();
		if (response == null || response.data() == null) {
			return new SendResult(expiredTokens, acceptedTickets);
		}
		for (int i = 0; i < Math.min(messages.size(), response.data().size()); i++) {
			Ticket ticket = response.data().get(i);
			String token = messages.get(i).to();
			if (isDeviceNotRegistered(ticket.details())) {
				expiredTokens.add(token);
			} else if (STATUS_OK.equals(ticket.status()) && ticket.id() != null) {
				acceptedTickets.add(new AcceptedTicket(ticket.id(), token));
			}
		}
		return new SendResult(expiredTokens, acceptedTickets);
	}

	/**
	 * 접수표 ID들의 전달 결과(receipt)를 조회해, Expo가 '등록되지 않은 기기'라고 알려 준 접수표와 결과가 나온 접수표를 돌려준다.
	 * 아직 결과가 없는 접수표는 어느 쪽에도 들어가지 않는다.
	 */
	public ReceiptResult findAllReceipt(List<String> ticketIds) {
		if (ticketIds.isEmpty()) {
			return new ReceiptResult(Set.of(), Set.of());
		}
		ReceiptResponse response = restClient.post()
				.uri(RECEIPTS_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("ids", ticketIds))
				.retrieve()
				.body(ReceiptResponse.class);
		Set<String> checked = new HashSet<>();
		Set<String> deviceNotRegistered = new HashSet<>();
		if (response == null || response.data() == null) {
			return new ReceiptResult(checked, deviceNotRegistered);
		}
		response.data().forEach((ticketId, receipt) -> {
			checked.add(ticketId);
			if (isDeviceNotRegistered(receipt.details())) {
				deviceNotRegistered.add(ticketId);
			}
		});
		return new ReceiptResult(checked, deviceNotRegistered);
	}

	private boolean isDeviceNotRegistered(Map<String, Object> details) {
		return details != null && DEVICE_NOT_REGISTERED.equals(details.get("error"));
	}

	public record ExpoPushMessage(
			String to,
			String title,
			String body,
			Map<String, Object> data,
			String sound
	) {
	}

	/**
	 * 보내자마자 '등록되지 않은 기기'로 거절된 토큰과, 접수돼 나중에 결과를 확인할 접수표.
	 */
	public record SendResult(List<String> expiredTokens, List<AcceptedTicket> acceptedTickets) {
	}

	public record AcceptedTicket(String ticketId, String token) {
	}

	/**
	 * checkedTicketIds: 결과가 나온 접수표, deviceNotRegisteredTicketIds: 그중 앱이 지워진 기기로 확인된 접수표.
	 */
	public record ReceiptResult(Set<String> checkedTicketIds, Set<String> deviceNotRegisteredTicketIds) {
	}

	private record SendResponse(List<Ticket> data) {
	}

	private record Ticket(String id, String status, String message, Map<String, Object> details) {
	}

	private record ReceiptResponse(Map<String, Receipt> data) {
	}

	private record Receipt(String status, String message, Map<String, Object> details) {
	}

}
