package com.deary.server.domain.report.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * OpenAI Moderation API로 글이나 사진 한 장을 검사한다.
 */
@Component
@EnableConfigurationProperties(ModerationProperties.class)
public class OpenAiModerationClient {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(20);

	private final ModerationProperties properties;
	private final RestClient restClient;

	public OpenAiModerationClient(ModerationProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + Objects.requireNonNullElse(properties.apiKey(), ""))
				.build();
	}

	public boolean configured() {
		return properties.configured();
	}

	public Result checkText(String text) {
		return check(List.of(Map.of("type", "text", "text", text)));
	}

	public Result checkImage(String imageUrl) {
		return check(List.of(Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))));
	}

	private Result check(List<Map<String, Object>> input) {
		ModerationResponse response = restClient.post()
				.uri(properties.baseUrl() + "/moderations")
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("model", properties.model(), "input", input))
				.retrieve()
				.body(ModerationResponse.class);
		if (response == null || response.results() == null || response.results().isEmpty()) {
			throw new IllegalStateException("검수 응답이 비어 있습니다.");
		}
		ModerationResponse.Item item = response.results().getFirst();
		List<String> flagged = item.categories() == null ? List.of() : item.categories().entrySet().stream()
				.filter(entry -> Boolean.TRUE.equals(entry.getValue()))
				.map(Map.Entry::getKey)
				.toList();
		return new Result(flagged, item.categoryScores() == null ? Map.of() : item.categoryScores());
	}

	/**
	 * @param flaggedCategories OpenAI가 걸렸다고 판단한 분류 (예: sexual, harassment)
	 * @param scores 분류별 점수(0~1)
	 */
	public record Result(
			List<String> flaggedCategories,
			Map<String, Double> scores
	) {
	}

	private record ModerationResponse(List<Item> results) {

		private record Item(
				Map<String, Boolean> categories,
				@JsonProperty("category_scores") Map<String, Double> categoryScores
		) {
		}

	}

}
