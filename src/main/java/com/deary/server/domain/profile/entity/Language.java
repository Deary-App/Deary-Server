package com.deary.server.domain.profile.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum Language {

	KO("ko"),
	JA("ja"),
	EN("en");

	private final String code;

	Language(String code) {
		this.code = code;
	}

	@JsonValue
	public String getCode() {
		return code;
	}

	@JsonCreator
	public static Language fromCode(String code) {
		return Arrays.stream(values())
				.filter(language -> language.code.equals(code))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("지원하지 않는 언어입니다: " + code));
	}

}
