package com.deary.server.domain.profile.exception;

import com.deary.server.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {

	PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND", "회원가입 정보를 찾을 수 없습니다."),
	TERMS_NOT_AGREED(HttpStatus.FORBIDDEN, "PROFILE_TERMS_NOT_AGREED", "필수 약관에 먼저 동의해 주세요."),
	UNDER_MINIMUM_AGE(HttpStatus.BAD_REQUEST, "PROFILE_UNDER_MINIMUM_AGE", "만 14세 미만은 가입할 수 없습니다."),
	SAME_LANGUAGE(HttpStatus.BAD_REQUEST, "PROFILE_SAME_LANGUAGE", "학습 언어는 모국어와 달라야 합니다."),
	INVALID_NATIONALITY(HttpStatus.BAD_REQUEST, "PROFILE_INVALID_NATIONALITY", "국적을 다시 선택해 주세요."),
	INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "PROFILE_INVALID_IMAGE", "프로필 사진을 다시 올려 주세요."),
	VERSION_REQUIRED(HttpStatus.BAD_REQUEST, "PROFILE_VERSION_REQUIRED", "수정하려면 현재 프로필 버전이 필요합니다."),
	VERSION_CONFLICT(HttpStatus.CONFLICT, "PROFILE_VERSION_CONFLICT", "다른 기기에서 먼저 수정했어요. 최신 정보를 불러온 뒤 다시 저장해 주세요.");

	private final HttpStatus status;
	private final String code;
	private final String message;

}
