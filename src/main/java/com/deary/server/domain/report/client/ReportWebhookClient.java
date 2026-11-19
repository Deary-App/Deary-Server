package com.deary.server.domain.report.client;

import com.deary.server.domain.report.entity.ReportSource;
import com.deary.server.domain.report.event.ReportRegisteredEvent;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@EnableConfigurationProperties(ReportNotificationProperties.class)
public class ReportWebhookClient {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
			.withZone(ZoneId.of("Asia/Seoul"));

	private final ReportNotificationProperties properties;
	private final RestClient restClient;

	public ReportWebhookClient(ReportNotificationProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.build();
	}

	public boolean configured() {
		return properties.configured();
	}

	public void sendReportRegistered(ReportRegisteredEvent event) {
		String title = event.source() == ReportSource.AUTO
				? "[Deary] AI 자동 검수에 걸린 항목이 있습니다"
				: "[Deary] 새 신고가 접수됐습니다";
		String message = """
				%s
				- 신고 ID: %d
				- 대상: %s #%d
				- 사유: %s
				- 접수 시각: %s (KST)
				24시간 안에 검토해 주세요.""".formatted(
				title,
				event.reportId(),
				event.targetType(),
				event.targetId(),
				event.reason(),
				TIME_FORMATTER.format(event.createdAt()));
		if (properties.hasAdminUrl()) {
			message += "\n처리하기: " + properties.adminUrl();
		}
		restClient.post()
				.uri(properties.webhookUrl())
				.contentType(MediaType.APPLICATION_JSON)
				.body(payload(message))
				.retrieve()
				.toBodilessEntity();
	}

	private Map<String, String> payload(String message) {
		if (properties.webhookUrl().contains("hooks.slack.com")) {
			return Map.of("text", message);
		}
		return Map.of("content", message);
	}

}
