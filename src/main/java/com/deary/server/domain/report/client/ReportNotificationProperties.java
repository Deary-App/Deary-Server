package com.deary.server.domain.report.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "report.notification")
public record ReportNotificationProperties(
		String webhookUrl
) {

	public boolean configured() {
		return webhookUrl != null && !webhookUrl.isBlank();
	}

}
