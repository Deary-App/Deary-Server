package com.deary.server.domain.correction.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CorrectionErrorCode implements ErrorCode {

	OWN_DIARY(HttpStatus.FORBIDDEN, "CORRECTION_OWN_DIARY", "내 기록은 교정할 수 없습니다."),
	ALREADY_CLAIMED(HttpStatus.CONFLICT, "CORRECTION_ALREADY_CLAIMED", "다른 사람이 먼저 교정하고 있어요. 잠시 후 다시 시도해 주세요."),
	ALREADY_COMPLETED(HttpStatus.CONFLICT, "CORRECTION_ALREADY_COMPLETED", "이미 교정이 끝난 기록입니다."),
	CLAIM_NOT_ACTIVE(HttpStatus.CONFLICT, "CORRECTION_CLAIM_NOT_ACTIVE", "교정할 수 있는 시간이 지났어요. 다시 교정하기를 눌러 주세요."),
	INCOMPLETE_ITEMS(HttpStatus.BAD_REQUEST, "CORRECTION_INCOMPLETE_ITEMS", "모든 문장에 응답해 주세요."),
	INVALID_ITEM(HttpStatus.BAD_REQUEST, "CORRECTION_INVALID_ITEM", "고친 문장을 입력해 주세요."),
	CORRECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CORRECTION_NOT_FOUND", "교정 결과가 아직 없습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
