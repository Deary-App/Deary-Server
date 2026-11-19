package com.deary.server.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * {@code X-Admin-Key} 헤더가 설정된 운영자 키와 같으면 운영자로 인증한다.
 * 서블릿 필터로 자동 등록되지 않도록 Bean으로 만들지 않고 운영자 SecurityFilterChain에만 넣는다.
 */
public class AdminKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Admin-Key";
	public static final String ROLE = "ADMIN";

	private final AdminKeyProperties properties;

	public AdminKeyAuthenticationFilter(AdminKeyProperties properties) {
		this.properties = properties;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String key = request.getHeader(HEADER);
		if (properties.configured() && key != null && matches(key)) {
			SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
					"admin", null, List.of(new SimpleGrantedAuthority("ROLE_" + ROLE))));
		}
		chain.doFilter(request, response);
	}

	private boolean matches(String key) {
		return MessageDigest.isEqual(
				key.getBytes(StandardCharsets.UTF_8),
				properties.apiKey().getBytes(StandardCharsets.UTF_8));
	}

}
