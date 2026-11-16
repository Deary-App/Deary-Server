package com.deary.server.domain.push.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Expo 푸시 서비스(https://exp.host)로 알림을 보낸다. Expo가 APNs로 전달한다.
 */
@Component
@EnableConfigurationProperties(ExpoPushProperties.class)
public class ExpoPushClient {

	private static final String SEND_URL = "https://exp.host/--/api/v2/push/send";
	private static final String DEVICE_NOT_REGISTERED = "DeviceNotRegistered";
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
	public List<String> send(List<ExpoPushMessage> messages) {
		if (messages.isEmpty()) {
			return List.of();
		}
		SendResponse response = restClient.post()
				.uri(SEND_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.body(messages)
				.retrieve()
				.body(SendResponse.class);
		List<String> expired = new ArrayList<>();
		if (response == null || response.data() == null) {
			return expired;
		}
		for (int i = 0; i < Math.min(messages.size(), response.data().size()); i++) {
			Ticket ticket = response.data().get(i);
			if (ticket.details() != null && DEVICE_NOT_REGISTERED.equals(ticket.details().get("error"))) {
				expired.add(messages.get(i).to());
			}
		}
		return expired;
	}

	public record ExpoPushMessage(
			String to,
			String title,
			String body,
			Map<String, Object> data,
			String sound
	) {
	}

	private record SendResponse(List<Ticket> data) {
	}

	private record Ticket(String status, String message, Map<String, Object> details) {
	}

}
