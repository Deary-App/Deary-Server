package com.deary.server.global.storage;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StorageErrorCode implements ErrorCode {

	STORAGE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE", "사진 저장소에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
