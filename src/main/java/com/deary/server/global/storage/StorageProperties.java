package com.deary.server.global.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.supabase")
public record StorageProperties(
		String url,
		String serviceRoleKey,
		String profileBucket,
		String diaryBucket,
		Duration downloadUrlExpiration
) {

	public boolean configured() {
		return url != null && !url.isBlank() && serviceRoleKey != null && !serviceRoleKey.isBlank();
	}

}
