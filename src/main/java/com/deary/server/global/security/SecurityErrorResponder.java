package com.deary.server.global.security;

import com.deary.server.global.exception.ErrorCode;
import com.deary.server.global.exception.dto.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

	private final ObjectMapper objectMapper;

	public void respond(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode)
			throws IOException {
		response.setStatus(errorCode.getStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		objectMapper.writeValue(response.getWriter(), ErrorDto.of(errorCode, request.getRequestURI()));
	}

}
