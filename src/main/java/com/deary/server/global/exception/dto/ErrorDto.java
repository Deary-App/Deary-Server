package com.deary.server.global.exception.dto;

import com.deary.server.global.exception.ErrorCode;
import java.time.Instant;

public record ErrorDto(
		String timestamp,
		int status,
		String errorCode,
		String message,
		String path
) {

	public static ErrorDto of(ErrorCode errorCode, String path) {
		return of(errorCode, errorCode.getMessage(), path);
	}

	public static ErrorDto of(ErrorCode errorCode, String message, String path) {
		return new ErrorDto(
				Instant.now().toString(),
				errorCode.getStatus().value(),
				errorCode.getCode(),
				message,
				path
		);
	}

}
