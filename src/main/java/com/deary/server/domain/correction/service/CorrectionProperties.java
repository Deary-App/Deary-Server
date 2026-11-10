package com.deary.server.domain.correction.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "correction")
public record CorrectionProperties(
		Duration claimDuration
) {
}
