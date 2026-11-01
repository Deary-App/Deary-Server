package com.deary.server.global.response;

import org.springframework.http.HttpStatus;

public record BaseResponse<T>(
		int status,
		String message,
		T data
) {

	public static <T> BaseResponse<T> of(HttpStatus status, String message, T data) {
		return new BaseResponse<>(status.value(), message, data);
	}

	public static <T> BaseResponse<T> ok(String message, T data) {
		return of(HttpStatus.OK, message, data);
	}

	public static BaseResponse<Void> ok(String message) {
		return of(HttpStatus.OK, message, null);
	}

}
