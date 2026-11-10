package com.deary.server.domain.user.service;

import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.exception.UserErrorCode;
import com.deary.server.domain.user.repository.UserRepository;
import com.deary.server.domain.upload.service.StoragePathPolicy;
import com.deary.server.global.exception.CustomException;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;
	private final StoragePathPolicy storagePathPolicy;

	@Transactional(readOnly = true)
	public User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
	}

	@Transactional
	public void removeUser(Long userId) {
		userRepository.deleteUserById(userId);
		storageClient.removeFolder(storageProperties.profileBucket(), storagePathPolicy.profileFolder(userId));
	}

}
