package com.deary.server.global.config;

import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.security.AdminKeyAuthenticationFilter;
import com.deary.server.global.security.AdminKeyProperties;
import com.deary.server.global.security.AuthUserAuthenticationConverter;
import com.deary.server.global.security.CustomAccessDeniedHandler;
import com.deary.server.global.security.CustomAuthenticationEntryPoint;
import com.deary.server.global.security.SecurityErrorResponder;
import com.deary.server.global.security.SuspendedUserFilter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminKeyProperties.class)
public class SecurityConfig {

	// 로그인·재발급·로그아웃은 액세스 토큰 없이 부른다.
	private static final String[] PUBLIC_AUTH_PATHS = {
			"/api/v1/auth/apple", "/api/v1/auth/google", "/api/v1/auth/reissue", "/api/v1/auth/logout"
	};

	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;
	private final AuthUserAuthenticationConverter authUserAuthenticationConverter;
	private final SecurityErrorResponder securityErrorResponder;
	private final AdminKeyProperties adminKeyProperties;
	private final UserService userService;

	/**
	 * 운영자 API는 앱 JWT 대신 운영자 키(X-Admin-Key)로만 인증한다.
	 */
	@Bean
	@Order(1)
	SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.securityMatcher("/api/v1/admin/**")
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(customAuthenticationEntryPoint)
						.accessDeniedHandler(customAccessDeniedHandler))
				.addFilterBefore(new AdminKeyAuthenticationFilter(adminKeyProperties), AnonymousAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth.anyRequest().hasRole(AdminKeyAuthenticationFilter.ROLE))
				.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(customAuthenticationEntryPoint)
						.accessDeniedHandler(customAccessDeniedHandler))
				.oauth2ResourceServer(oauth2 -> oauth2
						.bearerTokenResolver(bearerTokenResolver())
						.jwt(jwt -> jwt.jwtAuthenticationConverter(authUserAuthenticationConverter))
						.authenticationEntryPoint(customAuthenticationEntryPoint)
						.accessDeniedHandler(customAccessDeniedHandler))
				.addFilterAfter(new SuspendedUserFilter(userService, securityErrorResponder),
						BearerTokenAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
						.requestMatchers(HttpMethod.GET, "/admin/**").permitAll()
						.requestMatchers(HttpMethod.POST, PUBLIC_AUTH_PATHS).permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/terms", "/api/v1/terms/*").permitAll()
						.anyRequest().authenticated())
				.build();
	}

	/**
	 * 로그인·재발급·로그아웃 요청에 만료되거나 세션이 끊긴 액세스 토큰이 Authorization 헤더로 남아 있어도
	 * 토큰 검증 단계에서 401로 막히지 않도록, 이 경로에서는 Bearer 토큰을 읽지 않는다.
	 */
	private BearerTokenResolver bearerTokenResolver() {
		DefaultBearerTokenResolver defaultResolver = new DefaultBearerTokenResolver();
		Set<String> publicAuthPaths = Set.of(PUBLIC_AUTH_PATHS);
		return request -> HttpMethod.POST.matches(request.getMethod()) && publicAuthPaths.contains(request.getRequestURI())
				? null
				: defaultResolver.resolve(request);
	}

}
