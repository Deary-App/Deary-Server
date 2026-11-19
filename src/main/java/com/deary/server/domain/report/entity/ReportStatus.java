package com.deary.server.domain.report.entity;

public enum ReportStatus {
	PENDING,
	/** AI 자동 검수가 숨기고 운영자 확인을 기다린다 */
	AUTO_HIDDEN,
	DISMISSED,
	HIDDEN,
	SUSPENDED
}
