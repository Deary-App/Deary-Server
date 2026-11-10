package com.deary.server.domain.terms.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TermsErrorCode implements ErrorCode {

	TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_NOT_FOUND", "약관을 찾을 수 없습니다."),
	TERMS_NOT_CURRENT(HttpStatus.BAD_REQUEST, "TERMS_NOT_CURRENT", "현재 버전이 아닌 약관입니다. 약관을 다시 불러와 주세요."),
	DUPLICATE_TERMS(HttpStatus.BAD_REQUEST, "TERMS_DUPLICATE", "같은 약관이 중복으로 포함되어 있습니다."),
	REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS_REQUIRED_NOT_AGREED", "필수 약관에 모두 동의해야 합니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
