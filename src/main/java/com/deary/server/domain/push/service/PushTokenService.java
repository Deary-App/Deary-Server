package com.deary.server.domain.push.service;

import com.deary.server.domain.push.entity.PushLanguage;
import com.deary.server.domain.push.entity.PushToken;
import com.deary.server.domain.push.repository.PushTokenRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CommonErrorCode;
import com.deary.server.global.exception.CustomException;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenService {

	private final PushTokenRepository pushTokenRepository;
	private final UserService userService;

	/**
	 * 이 기기의 푸시 토큰을 내 계정에 등록한다. 이미 있는 토큰이면 주인과 언어를 지금 값으로 바꾼다(같은 기기에서 계정을 바꾼 경우).
	 */
	@Transactional
	public void registerPushToken(Long userId, String token, PushLanguage language) {
		User user = userService.findUser(userId);
		pushTokenRepository.findPushTokenByToken(token).ifPresentOrElse(
				pushToken -> pushToken.assign(user, language),
				() -> savePushToken(user, token, language));
	}

	/**
	 * 로그아웃할 때 이 기기의 토큰을 지운다. 내 토큰이 아니거나 이미 없어도 성공으로 본다.
	 */
	@Transactional
	public void removePushToken(Long userId, String token) {
		pushTokenRepository.deletePushTokenByTokenAndUserId(token, userId);
	}

	@Transactional(readOnly = true)
	public List<PushToken> findAllPushToken(Long userId) {
		return pushTokenRepository.findAllPushTokenByUserId(userId);
	}

	/**
	 * Expo가 더 이상 쓸 수 없다고 알려 준 토큰(앱 삭제 등)을 지운다.
	 */
	@Transactional
	public void removeAllExpiredPushToken(Collection<String> tokens) {
		if (!tokens.isEmpty()) {
			pushTokenRepository.deleteAllPushTokenByTokenIn(tokens);
		}
	}

	// 같은 토큰이 동시에 두 번 등록되면 unique 제약으로 한 건만 저장된다.
	private void savePushToken(User user, String token, PushLanguage language) {
		try {
			pushTokenRepository.saveAndFlush(PushToken.create(user, token, language));
		} catch (DataIntegrityViolationException exception) {
			throw new CustomException(CommonErrorCode.CONFLICT);
		}
	}

}
