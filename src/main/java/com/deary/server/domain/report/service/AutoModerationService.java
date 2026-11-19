package com.deary.server.domain.report.service;

import com.deary.server.domain.report.client.ModerationProperties;
import com.deary.server.domain.report.client.OpenAiModerationClient;
import com.deary.server.domain.report.entity.ReportReason;
import com.deary.server.domain.report.entity.ReportTargetType;
import com.deary.server.domain.report.service.ReportModerationService.AutoModerationTarget;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 올라온 글·사진을 OpenAI Moderation으로 검사해, 숨김 분류에 걸리면 자동으로 숨기고 신고함에 올린다.
 * 외부 API 호출 중에는 트랜잭션을 잡지 않는다. 검사에 실패하면 숨기지 않고 넘어간다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoModerationService {

	private static final int DETAIL_MAX_LENGTH = 500;
	private static final String SEXUAL = "sexual";

	private final OpenAiModerationClient moderationClient;
	private final ModerationProperties moderationProperties;
	private final ReportModerationService reportModerationService;

	public void moderate(ReportTargetType targetType, Long targetId) {
		if (!moderationClient.configured()) {
			return;
		}
		try {
			Optional<AutoModerationTarget> target = reportModerationService.findAutoModerationTarget(targetType, targetId);
			if (target.isEmpty()) {
				return;
			}
			Map<String, Double> hits = check(target.get());
			if (hits.isEmpty()) {
				return;
			}
			reportModerationService.registerAutoReport(targetType, targetId, toReason(hits.keySet()), detail(hits));
			log.info("자동 검수 숨김 targetType={} targetId={} categories={}", targetType, targetId, hits.keySet());
		} catch (RuntimeException exception) {
			log.warn("자동 검수 실패 targetType={} targetId={} error={}", targetType, targetId,
					exception.getClass().getSimpleName());
		}
	}

	// 숨김 분류에 걸린 분류와 가장 높은 점수. 글과 사진 중 하나라도 걸리면 숨긴다.
	private Map<String, Double> check(AutoModerationTarget target) {
		List<OpenAiModerationClient.Result> results = new ArrayList<>();
		if (target.text() != null && !target.text().isBlank()) {
			results.add(moderationClient.checkText(target.text()));
		}
		target.imageUrls().forEach(url -> results.add(moderationClient.checkImage(url)));
		List<String> hideCategories = moderationProperties.hideCategories() == null
				? List.of() : moderationProperties.hideCategories();
		Map<String, Double> hits = new LinkedHashMap<>();
		for (OpenAiModerationClient.Result result : results) {
			Set<String> categories = new LinkedHashSet<>(result.flaggedCategories());
			result.scores().forEach((category, score) -> {
				if (category.startsWith(SEXUAL) && score >= moderationProperties.sexualScoreThreshold()) {
					categories.add(category);
				}
			});
			categories.stream()
					.filter(hideCategories::contains)
					.forEach(category -> hits.merge(category, result.scores().getOrDefault(category, 0.0), Math::max));
		}
		return hits;
	}

	private ReportReason toReason(Set<String> categories) {
		if (categories.stream().anyMatch(category -> category.startsWith(SEXUAL))) {
			return ReportReason.SEXUAL;
		}
		if (categories.stream().anyMatch(category -> category.startsWith("harassment") || category.startsWith("hate"))) {
			return ReportReason.ABUSE;
		}
		return ReportReason.OTHER;
	}

	private String detail(Map<String, Double> hits) {
		String detail = "AI 자동 검수: " + hits.entrySet().stream()
				.map(entry -> "%s %.2f".formatted(entry.getKey(), entry.getValue()))
				.collect(Collectors.joining(", "));
		return detail.length() > DETAIL_MAX_LENGTH ? detail.substring(0, DETAIL_MAX_LENGTH) : detail;
	}

}
