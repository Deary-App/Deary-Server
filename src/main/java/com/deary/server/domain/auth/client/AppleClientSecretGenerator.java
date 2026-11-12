package com.deary.server.domain.auth.client;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * Apple /auth/token·/auth/revoke 요청에 쓰는 client_secret(ES256 JWT)을 만든다.
 */
final class AppleClientSecretGenerator {

	private static final String AUDIENCE = "https://appleid.apple.com";
	private static final Duration LIFETIME = Duration.ofMinutes(5);

	private final String teamId;
	private final String keyId;
	private final ECPrivateKey privateKey;

	AppleClientSecretGenerator(String teamId, String keyId, String privateKeyPem) {
		this.teamId = teamId;
		this.keyId = keyId;
		this.privateKey = parsePrivateKey(privateKeyPem);
	}

	String generate(String clientId) {
		Instant now = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.issuer(teamId)
				.issueTime(Date.from(now))
				.expirationTime(Date.from(now.plus(LIFETIME)))
				.audience(AUDIENCE)
				.subject(clientId)
				.build();
		SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build(), claims);
		try {
			jwt.sign(new ECDSASigner(privateKey));
		} catch (JOSEException exception) {
			throw new IllegalStateException("Apple client secret 서명에 실패했습니다.", exception);
		}
		return jwt.serialize();
	}

	private static ECPrivateKey parsePrivateKey(String pem) {
		String base64 = pem.replace("\\n", "\n")
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");
		try {
			byte[] der = Base64.getDecoder().decode(base64);
			return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(der));
		} catch (IllegalArgumentException | GeneralSecurityException | ClassCastException exception) {
			throw new IllegalStateException("APPLE_PRIVATE_KEY 형식이 올바르지 않습니다.");
		}
	}

}
