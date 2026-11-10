package com.deary.server.domain.diary.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiaryErrorCode implements ErrorCode {

	DIARY_NOT_FOUND(HttpStatus.NOT_FOUND, "DIARY_NOT_FOUND", "기록을 찾을 수 없습니다."),
	ALREADY_WRITTEN_TODAY(HttpStatus.CONFLICT, "DIARY_ALREADY_WRITTEN_TODAY", "오늘은 이미 기록을 남겼어요."),
	INVALID_PHOTO(HttpStatus.BAD_REQUEST, "DIARY_INVALID_PHOTO", "사진을 다시 올려 주세요."),
	EMPTY_CONTENT(HttpStatus.BAD_REQUEST, "DIARY_EMPTY_CONTENT", "본문을 입력해 주세요."),
	INVALID_MONTH(HttpStatus.BAD_REQUEST, "DIARY_INVALID_MONTH", "조회할 연도와 월을 다시 확인해 주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
