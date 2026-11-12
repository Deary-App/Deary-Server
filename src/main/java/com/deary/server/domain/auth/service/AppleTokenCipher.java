package com.deary.server.domain.auth.service;

import com.deary.server.global.jwt.JwtProperties;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Apple refresh token을 DB에 저장하기 전에 AES-256-GCM으로 암호화한다.
 * 키는 JWT_SECRET에서 HMAC-SHA256으로 파생하므로, JWT_SECRET을 바꾸면 기존 값은 복호화할 수 없다.
 */
@Component
public class AppleTokenCipher {

	private static final String VERSION_PREFIX = "v1:";
	private static final String KEY_LABEL = "deary-apple-refresh-token";
	private static final int IV_BYTES = 12;
	private static final int TAG_BITS = 128;

	private final SecretKey key;
	private final SecureRandom secureRandom = new SecureRandom();

	public AppleTokenCipher(JwtProperties jwtProperties) {
		this.key = deriveKey(jwtProperties.secret());
	}

	public String encrypt(String plainText) {
		try {
			byte[] iv = new byte[IV_BYTES];
			secureRandom.nextBytes(iv);
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] payload = ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
			return VERSION_PREFIX + Base64.getEncoder().encodeToString(payload);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Apple 토큰 암호화에 실패했습니다.", exception);
		}
	}

	public String decrypt(String cipherText) {
		if (cipherText == null || !cipherText.startsWith(VERSION_PREFIX)) {
			throw new IllegalStateException("지원하지 않는 Apple 토큰 형식입니다.");
		}
		try {
			byte[] payload = Base64.getDecoder().decode(cipherText.substring(VERSION_PREFIX.length()));
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, payload, 0, IV_BYTES));
			byte[] decrypted = cipher.doFinal(payload, IV_BYTES, payload.length - IV_BYTES);
			return new String(decrypted, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException | GeneralSecurityException exception) {
			throw new IllegalStateException("Apple 토큰 복호화에 실패했습니다.");
		}
	}

	private static SecretKey deriveKey(String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			return new SecretKeySpec(mac.doFinal(KEY_LABEL.getBytes(StandardCharsets.UTF_8)), "AES");
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException("Apple 토큰 암호화 키를 만들 수 없습니다.", exception);
		}
	}

}
