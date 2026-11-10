package com.deary.server.domain.auth.client;

import com.deary.server.domain.auth.exception.AuthErrorCode;
import com.deary.server.domain.user.entity.SocialProvider;
import com.deary.server.global.exception.CustomException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableConfigurationProperties(SocialLoginProperties.class)
public class SocialIdTokenVerifier {

	private final JwtDecoder appleDecoder;
	private final JwtDecoder googleDecoder;

	public SocialIdTokenVerifier(SocialLoginProperties properties) {
		SocialLoginProperties.Apple apple = properties.apple();
		SocialLoginProperties.Google google = properties.google();
		this.appleDecoder = IdTokenDecoderFactory.create(
				apple.jwkSetUri(), List.of(apple.issuer()), apple.clientIds());
		this.googleDecoder = IdTokenDecoderFactory.create(
				google.jwkSetUri(), google.issuers(), google.clientIds());
	}

	public SocialUserInfo verifyApple(String identityToken, String rawNonce) {
		Jwt jwt = decode(appleDecoder, identityToken, SocialProvider.APPLE);
		String nonce = jwt.getClaimAsString("nonce");
		if (nonce == null || !nonce.equals(sha256Hex(rawNonce))) {
			throw new CustomException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
		}
		return toUserInfo(SocialProvider.APPLE, jwt);
	}

	public SocialUserInfo verifyGoogle(String idToken) {
		Jwt jwt = decode(googleDecoder, idToken, SocialProvider.GOOGLE);
		return toUserInfo(SocialProvider.GOOGLE, jwt);
	}

	private Jwt decode(JwtDecoder decoder, String token, SocialProvider provider) {
		try {
			return decoder.decode(token);
		} catch (BadJwtException exception) {
			throw new CustomException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
		} catch (JwtException exception) {
			log.warn("소셜 ID 토큰 검증 실패: provider={}, reason={}", provider, exception.getClass().getSimpleName());
			throw new CustomException(AuthErrorCode.SOCIAL_PROVIDER_UNAVAILABLE);
		}
	}

	private SocialUserInfo toUserInfo(SocialProvider provider, Jwt jwt) {
		String subject = jwt.getSubject();
		if (subject == null || subject.isBlank()) {
			throw new CustomException(AuthErrorCode.INVALID_SOCIAL_TOKEN);
		}
		return new SocialUserInfo(provider, subject, verifiedEmail(provider, jwt));
	}

	private String verifiedEmail(SocialProvider provider, Jwt jwt) {
		if (provider == SocialProvider.GOOGLE && !Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"))) {
			return null;
		}
		return jwt.getClaimAsString("email");
	}

	private String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
		}
	}

}
