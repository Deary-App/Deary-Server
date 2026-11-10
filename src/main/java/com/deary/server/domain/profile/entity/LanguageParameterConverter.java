package com.deary.server.domain.profile.entity;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LanguageParameterConverter implements Converter<String, Language> {

	@Override
	public Language convert(String code) {
		return Language.fromCode(code);
	}

}
