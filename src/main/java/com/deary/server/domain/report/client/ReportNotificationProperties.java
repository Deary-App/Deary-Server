package com.deary.server.domain.report.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "report.notification")
public record ReportNotificationProperties(
		String webhookUrl,
		String adminUrl
) {

	public boolean configured() {
		return webhookUrl != null && !webhookUrl.isBlank();
	}

	public boolean hasAdminUrl() {
		return adminUrl != null && !adminUrl.isBlank();
	}

}
