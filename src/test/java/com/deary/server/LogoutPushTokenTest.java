package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.deary.server.domain.auth.dto.request.LogoutRequest;
import com.deary.server.domain.auth.service.AuthService;
import com.deary.server.domain.auth.service.TokenService;
import com.deary.server.domain.push.entity.PushLanguage;
import com.deary.server.domain.push.repository.PushTokenRepository;
import com.deary.server.domain.push.service.PushTokenService;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class LogoutPushTokenTest {

	private static final String MY_TOKEN = "ExponentPushToken[mine]";
	private static final String OTHER_TOKEN = "ExponentPushToken[other]";

	@Autowired
	private AuthService authService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PushTokenService pushTokenService;

	@Autowired
	private PushTokenRepository pushTokenRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	void 로그아웃할_때_보낸_내_푸시_토큰을_함께_지운다() {
		User me = userRepository.save(User.create("me@example.com"));
		pushTokenService.registerPushToken(me.getId(), MY_TOKEN, PushLanguage.KO);
		String refreshToken = tokenService.generateTokens(me.getId()).refreshToken();

		authService.handleLogout(new LogoutRequest(refreshToken, MY_TOKEN));

		assertThat(pushTokenRepository.findPushTokenByToken(MY_TOKEN)).isEmpty();
	}

	@Test
	void 다른_사람의_푸시_토큰은_지우지_않는다() {
		User me = userRepository.save(User.create("me@example.com"));
		User other = userRepository.save(User.create("other@example.com"));
		pushTokenService.registerPushToken(other.getId(), OTHER_TOKEN, PushLanguage.KO);
		String refreshToken = tokenService.generateTokens(me.getId()).refreshToken();

		authService.handleLogout(new LogoutRequest(refreshToken, OTHER_TOKEN));

		assertThat(pushTokenRepository.findPushTokenByToken(OTHER_TOKEN)).isPresent();
	}

	@Test
	void 푸시_토큰을_보내지_않으면_푸시_토큰은_그대로다() {
		User me = userRepository.save(User.create("me@example.com"));
		pushTokenService.registerPushToken(me.getId(), MY_TOKEN, PushLanguage.KO);
		String refreshToken = tokenService.generateTokens(me.getId()).refreshToken();

		authService.handleLogout(new LogoutRequest(refreshToken, null));

		assertThat(pushTokenRepository.findPushTokenByToken(MY_TOKEN)).isPresent();
	}

	@Test
	void 없는_리프레시_토큰이면_푸시_토큰을_지우지_않는다() {
		User me = userRepository.save(User.create("me@example.com"));
		pushTokenService.registerPushToken(me.getId(), MY_TOKEN, PushLanguage.KO);

		authService.handleLogout(new LogoutRequest("unknown-refresh-token", MY_TOKEN));

		assertThat(pushTokenRepository.findPushTokenByToken(MY_TOKEN)).isPresent();
	}

}
