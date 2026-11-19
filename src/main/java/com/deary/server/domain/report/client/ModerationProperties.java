package com.deary.server.domain.report.client;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 자동 검수(OpenAI Moderation) 설정. API 키가 비어 있으면 검사하지 않는다.
 * hideCategories에 든 분류에 걸리면 자동으로 숨긴다. 선정성(sexual)은 OpenAI 판정에 더해
 * 점수가 sexualScoreThreshold 이상이어도 걸린 것으로 본다.
 */
@ConfigurationProperties(prefix = "moderation.openai")
public record ModerationProperties(
		String baseUrl,
		String apiKey,
		String model,
		List<String> hideCategories,
		double sexualScoreThreshold
) {

	public boolean configured() {
		return apiKey != null && !apiKey.isBlank();
	}

}
