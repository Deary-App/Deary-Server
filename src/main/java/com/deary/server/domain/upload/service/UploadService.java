package com.deary.server.domain.upload.service;

import com.deary.server.domain.upload.dto.response.DiaryImageUploadIssueResponse;
import com.deary.server.domain.upload.dto.response.UploadUrlIssueResponse;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.storage.StorageProperties;
import com.deary.server.global.storage.SupabaseStorageClient;
import java.util.ArrayList;
import java.util.List;
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

	public DiaryImageUploadIssueResponse generateDiaryImageUploadUrls(Long userId, int count) {
		userService.findUser(userId);
		List<UploadUrlIssueResponse> uploads = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			String path = storagePathPolicy.generateDiaryImagePath(userId);
			uploads.add(new UploadUrlIssueResponse(path, storageClient.createUploadUrl(storageProperties.diaryBucket(), path)));
		}
		return new DiaryImageUploadIssueResponse(uploads);
	}

}
