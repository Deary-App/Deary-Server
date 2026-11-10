package com.deary.server.domain.auth.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_INVALID_SOCIAL_TOKEN", "소셜 로그인 정보를 확인할 수 없습니다."),
	SOCIAL_PROVIDER_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AUTH_SOCIAL_PROVIDER_UNAVAILABLE", "소셜 로그인 서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
