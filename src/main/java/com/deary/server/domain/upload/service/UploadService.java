package com.deary.server.domain.upload.service;

import com.deary.server.domain.upload.dto.response.UploadUrlIssueResponse;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadService {

	private final SupabaseStorageClient storageClient;
	private final StorageProperties storageProperties;
	private final StoragePathPolicy storagePathPolicy;
	private final UserService userService;

	public UploadUrlIssueResponse generateProfileImageUploadUrl(Long userId) {
		userService.findUser(userId);
		String path = storagePathPolicy.generateProfileImagePath(userId);
		String uploadUrl = storageClient.createUploadUrl(storageProperties.profileBucket(), path);
		return new UploadUrlIssueResponse(path, uploadUrl);
	}

}
