package com.deary.server.domain.report.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReportErrorCode implements ErrorCode {

	SELF_REPORT(HttpStatus.BAD_REQUEST, "REPORT_SELF", "나 자신이나 내가 쓴 글은 신고할 수 없습니다."),
	TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_TARGET_NOT_FOUND", "신고할 대상을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
