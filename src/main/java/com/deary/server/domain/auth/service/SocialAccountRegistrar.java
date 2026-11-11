package com.deary.server.domain.auth.service;

import com.deary.server.domain.auth.client.SocialUserInfo;
import com.deary.server.domain.user.entity.SocialAccount;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.SocialAccountRepository;
import com.deary.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SocialAccountRegistrar {

	private final UserRepository userRepository;
	private final SocialAccountRepository socialAccountRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Long register(SocialUserInfo userInfo) {
		User user = userRepository.save(User.create(userInfo.email()));
		socialAccountRepository.saveAndFlush(
				SocialAccount.create(userInfo.provider(), userInfo.providerUserId(), user)
		);
		return user.getId();
	}

}
