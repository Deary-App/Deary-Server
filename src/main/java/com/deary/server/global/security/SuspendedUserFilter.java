package com.deary.server.global.security;

import com.deary.server.domain.user.exception.UserErrorCode;
import com.deary.server.domain.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 운영자가 정지한 사용자의 인증 요청을 403(USER_SUSPENDED)으로 거절한다.
 * 서블릿 필터로 자동 등록되지 않도록 Bean으로 만들지 않고 SecurityConfig에서만 넣는다.
 */
public class SuspendedUserFilter extends OncePerRequestFilter {

	private final UserService userService;
	private final SecurityErrorResponder securityErrorResponder;

	public SuspendedUserFilter(UserService userService, SecurityErrorResponder securityErrorResponder) {
		this.userService = userService;
		this.securityErrorResponder = securityErrorResponder;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof AuthUser authUser
				&& userService.isSuspended(authUser.userId())) {
			securityErrorResponder.respond(request, response, UserErrorCode.USER_SUSPENDED);
			return;
		}
		chain.doFilter(request, response);
	}

}
