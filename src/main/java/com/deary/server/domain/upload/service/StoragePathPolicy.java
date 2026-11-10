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
		String folder = profileFolder(userId);
		return path.startsWith(folder)
				&& path.length() > folder.length()
				&& path.substring(folder.length()).matches("[0-9a-f-]{36}");
	}

}
