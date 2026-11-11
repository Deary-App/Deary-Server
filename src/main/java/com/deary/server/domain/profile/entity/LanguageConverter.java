package com.deary.server.domain.profile.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguageConverter implements AttributeConverter<Language, String> {

	@Override
	public String convertToDatabaseColumn(Language language) {
		return language == null ? null : language.getCode();
	}

	@Override
	public Language convertToEntityAttribute(String code) {
		return code == null ? null : Language.fromCode(code);
	}

}
