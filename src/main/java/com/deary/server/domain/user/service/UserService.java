package com.deary.server.domain.user.service;

import com.deary.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public void removeUser(Long userId) {
		userRepository.deleteUserById(userId);
	}

}
