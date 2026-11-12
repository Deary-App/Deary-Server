package com.deary.server.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Sign in with Apple 토큰 교환(/auth/token)과 해지(/auth/revoke)를 호출한다.
 * 토큰·client_secret 값은 로그에 남기지 않는다.
 */
@Slf4j
@Component
public class AppleTokenClient {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final SocialLoginProperties.Apple properties;
	private final AppleClientSecretGenerator secretGenerator;
	private final RestClient restClient;

	public AppleTokenClient(SocialLoginProperties properties) {
		this.properties = properties.apple();
		this.secretGenerator = createSecretGenerator(this.properties);
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	public boolean configured() {
		return secretGenerator != null;
	}

	/**
	 * authorization code를 refresh token으로 교환한다. 실패하면 빈 값을 돌려준다.
	 */
	public Optional<String> exchangeAuthorizationCode(String authorizationCode, String clientId) {
		if (!configured()) {
			return Optional.empty();
		}
		MultiValueMap<String, String> form = baseForm(clientId);
		form.add("code", authorizationCode);
		form.add("grant_type", "authorization_code");
		try {
			TokenResponse response = restClient.post()
					.uri(properties.tokenUri())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form)
					.retrieve()
					.body(TokenResponse.class);
			if (response == null || response.refreshToken() == null || response.refreshToken().isBlank()) {
				log.warn("Apple 토큰 교환 응답에 refresh token이 없습니다.");
				return Optional.empty();
			}
			return Optional.of(response.refreshToken());
		} catch (RestClientException exception) {
			log.warn("Apple 토큰 교환 실패: reason={}", describe(exception));
			return Optional.empty();
		}
	}

	/**
	 * refresh token을 해지한다. 성공 여부만 돌려주고 예외를 던지지 않는다.
	 */
	public boolean revokeRefreshToken(String refreshToken, String clientId) {
		if (!configured()) {
			return false;
		}
		MultiValueMap<String, String> form = baseForm(clientId);
		form.add("token", refreshToken);
		form.add("token_type_hint", "refresh_token");
		try {
			restClient.post()
					.uri(properties.revokeUri())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form)
					.retrieve()
					.toBodilessEntity();
			return true;
		} catch (RestClientException exception) {
			log.warn("Apple 토큰 해지 실패: reason={}", describe(exception));
			return false;
		}
	}

	private MultiValueMap<String, String> baseForm(String clientId) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", clientId);
		form.add("client_secret", secretGenerator.generate(clientId));
		return form;
	}

	private static String describe(RestClientException exception) {
		if (exception instanceof RestClientResponseException responseException) {
			return "status=" + responseException.getStatusCode().value();
		}
		return exception.getClass().getSimpleName();
	}

	private static AppleClientSecretGenerator createSecretGenerator(SocialLoginProperties.Apple properties) {
		if (!properties.revokeConfigured()) {
			log.warn("Apple 토큰 해지 설정(APPLE_TEAM_ID, APPLE_KEY_ID, APPLE_PRIVATE_KEY)이 비어 있어 탈퇴 시 Apple 연결 해지를 건너뜁니다.");
			return null;
		}
		try {
			return new AppleClientSecretGenerator(properties.teamId(), properties.keyId(), properties.privateKey());
		} catch (IllegalStateException exception) {
			log.warn("Apple 토큰 해지 설정을 읽지 못해 탈퇴 시 Apple 연결 해지를 건너뜁니다: {}", exception.getMessage());
			return null;
		}
	}

	private record TokenResponse(@JsonProperty("refresh_token") String refreshToken) {
	}

}
