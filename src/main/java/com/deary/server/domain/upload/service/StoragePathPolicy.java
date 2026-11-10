package com.deary.server.domain.upload.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StoragePathPolicy {

	public String generateProfileImagePath(Long userId) {
		return profileFolder(userId) + UUID.randomUUID();
	}

	public String profileFolder(Long userId) {
		return "profiles/" + userId + "/";
	}

	public boolean isOwnedProfileImagePath(Long userId, String path) {
		return isGeneratedPathIn(profileFolder(userId), path);
	}

	public String generateDiaryImagePath(Long userId) {
		return diaryFolder(userId) + UUID.randomUUID();
	}

	public String diaryFolder(Long userId) {
		return "diaries/" + userId + "/";
	}

	public boolean isOwnedDiaryImagePath(Long userId, String path) {
		return isGeneratedPathIn(diaryFolder(userId), path);
	}

	private boolean isGeneratedPathIn(String folder, String path) {
		return path.startsWith(folder)
				&& path.length() > folder.length()
				&& path.substring(folder.length()).matches("[0-9a-f-]{36}");
	}

}
