package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.deary.server.domain.auth.dto.response.TokenIssueResponse;
import com.deary.server.domain.auth.entity.RefreshToken;
import com.deary.server.domain.auth.repository.RefreshTokenRepository;
import com.deary.server.domain.auth.service.RefreshTokenHasher;
import com.deary.server.domain.auth.service.TokenService;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.jwt.AccessTokenProvider;
import com.deary.server.global.jwt.JwtProperties;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccessTokenSessionTest {

	private static final String PROTECTED_PATH = "/api/v1/users/me/blocks";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private AccessTokenProvider accessTokenProvider;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtProperties jwtProperties;

	@Autowired
	private RefreshTokenHasher refreshTokenHasher;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void 로그인한_액세스_토큰으로_요청할_수_있다() throws Exception {
		TokenIssueResponse tokens = tokenService.generateTokens(saveUser("me").getId());

		callProtected(tokens.accessToken(), 200);
	}

	@Test
	void 로그아웃하면_그_기기의_액세스_토큰이_바로_거절된다() throws Exception {
		TokenIssueResponse tokens = tokenService.generateTokens(saveUser("me").getId());

		logout(tokens.refreshToken());

		callProtected(tokens.accessToken(), 401);
	}

	@Test
	void 다른_기기에서_로그아웃해도_이_기기의_액세스_토큰은_그대로다() throws Exception {
		User me = saveUser("me");
		TokenIssueResponse phone = tokenService.generateTokens(me.getId());
		TokenIssueResponse tablet = tokenService.generateTokens(me.getId());

		logout(tablet.refreshToken());

		callProtected(phone.accessToken(), 200);
		callProtected(tablet.accessToken(), 401);
	}

	@Test
	void 재발급해도_세션이_유지돼_이전_액세스_토큰도_쓸_수_있고_이전_리프레시_토큰은_거절된다() throws Exception {
		TokenIssueResponse before = tokenService.generateTokens(saveUser("me").getId());

		TokenIssueResponse after = tokenService.handleReissue(before.refreshToken());

		callProtected(before.accessToken(), 200);
		callProtected(after.accessToken(), 200);
		assertThatThrownBy(() -> tokenService.handleReissue(before.refreshToken()))
				.isInstanceOf(CustomException.class);
	}

	@Test
	void 탈퇴하면_액세스_토큰이_바로_거절된다() throws Exception {
		User me = saveUser("me");
		TokenIssueResponse tokens = tokenService.generateTokens(me.getId());

		// 탈퇴와 같은 DB 삭제(FK on delete cascade로 세션도 함께 지워진다). 저장소 파일 삭제는 이 테스트와 상관없어 뺐다.
		userRepository.deleteUserById(me.getId());

		callProtected(tokens.accessToken(), 401);
	}

	@Test
	void 세션_ID가_없는_예전_액세스_토큰은_거절된다() throws Exception {
		User me = saveUser("me");
		tokenService.generateTokens(me.getId());

		callProtected(accessTokenWithoutSessionId(me.getId()), 401);
	}

	@Test
	void 다른_사용자의_세션_ID를_넣은_액세스_토큰은_거절된다() throws Exception {
		User me = saveUser("me");
		User other = saveUser("other");
		tokenService.generateTokens(me.getId());
		Long otherSessionId = refreshTokenRepository.findAll().stream()
				.filter(token -> token.getUser().getId().equals(other.getId()))
				.findFirst()
				.map(RefreshToken::getId)
				.orElseGet(() -> sessionIdOf(tokenService.generateTokens(other.getId())));

		callProtected(accessTokenProvider.generateAccessToken(me.getId(), otherSessionId), 401);
	}

	@Test
	void 재발급_요청에_끊긴_액세스_토큰이_헤더로_남아_있어도_재발급된다() throws Exception {
		User me = saveUser("me");
		TokenIssueResponse tokens = tokenService.generateTokens(me.getId());

		mockMvc.perform(post("/api/v1/auth/reissue")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessTokenWithoutSessionId(me.getId()))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + tokens.refreshToken() + "\"}"))
				.andExpect(status().isOk());
	}

	@Test
	void 만료_세션은_조회_뒤_다른_요청이_재발급했으면_지우지_않는다() {
		User me = saveUser("me");
		RefreshToken expired = refreshTokenRepository.saveAndFlush(RefreshToken.create(
				refreshTokenHasher.hash("old"), Instant.now().minus(Duration.ofMinutes(1)), me));
		Instant now = Instant.now();

		int deletedWithChangedHash = refreshTokenRepository.deleteExpiredRefreshToken(
				expired.getId(), refreshTokenHasher.hash("already-rotated"), now);
		int deletedWithSameHash = refreshTokenRepository.deleteExpiredRefreshToken(
				expired.getId(), refreshTokenHasher.hash("old"), now);

		assertThat(deletedWithChangedHash).isZero();
		assertThat(deletedWithSameHash).isEqualTo(1);
	}

	private User saveUser(String name) {
		return userRepository.save(User.create(name + "@example.com"));
	}

	private Long sessionIdOf(TokenIssueResponse tokens) {
		return refreshTokenRepository.findRefreshTokenByTokenHash(refreshTokenHasher.hash(tokens.refreshToken()))
				.orElseThrow()
				.getId();
	}

	private String accessTokenWithoutSessionId(Long userId) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtProperties.issuer())
				.subject(String.valueOf(userId))
				.issuedAt(now)
				.expiresAt(now.plus(jwtProperties.accessTokenExpiration()))
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}

	private void callProtected(String accessToken, int expectedStatus) throws Exception {
		mockMvc.perform(get(PROTECTED_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
				.andExpect(status().is(expectedStatus));
	}

	private void logout(String refreshToken) throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isOk());
	}

}
