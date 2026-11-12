package com.deary.server.domain.block.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BlockErrorCode implements ErrorCode {

	SELF_BLOCK(HttpStatus.BAD_REQUEST, "BLOCK_SELF", "나 자신은 차단할 수 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
