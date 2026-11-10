package com.deary.server.global.storage;

import com.deary.server.global.exception.CustomException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@EnableConfigurationProperties(StorageProperties.class)
public class SupabaseStorageClient {

	private static final String STORAGE_PATH = "/storage/v1";
	private static final int LIST_LIMIT = 1000;

	private final StorageProperties properties;
	private final RestClient restClient;

	public SupabaseStorageClient(StorageProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(properties.url()) + STORAGE_PATH)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.serviceRoleKey())
				.defaultHeader("apikey", properties.serviceRoleKey())
				.build();
	}

	public String createUploadUrl(String bucket, String path) {
		ensureConfigured();
		SignedUploadResponse response = call("upload-url", () -> restClient.post()
				.uri("/object/upload/sign/" + bucket + "/" + path)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of())
				.retrieve()
				.body(SignedUploadResponse.class));
		return absoluteUrl(response.url());
	}

	public String createDownloadUrl(String bucket, String path) {
		ensureConfigured();
		SignedDownloadResponse response = call("download-url", () -> restClient.post()
				.uri("/object/sign/" + bucket + "/" + path)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("expiresIn", properties.downloadUrlExpiration().toSeconds()))
				.retrieve()
				.body(SignedDownloadResponse.class));
		return absoluteUrl(response.signedURL());
	}

	public String publicUrl(String bucket, String path) {
		if (path == null || !properties.configured()) {
			return null;
		}
		return trimTrailingSlash(properties.url()) + STORAGE_PATH + "/object/public/" + bucket + "/" + path;
	}

	public void removeFolder(String bucket, String folder) {
		if (!properties.configured()) {
			return;
		}
		try {
			StoredObject[] objects = restClient.post()
					.uri("/object/list/" + bucket)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("prefix", folder, "limit", LIST_LIMIT))
					.retrieve()
					.body(StoredObject[].class);
			if (objects == null || objects.length == 0) {
				return;
			}
			List<String> paths = Arrays.stream(objects).map(object -> folder + object.name()).toList();
			restClient.method(HttpMethod.DELETE)
					.uri("/object/" + bucket)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("prefixes", paths))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientException exception) {
			log.warn("Storage 파일 삭제 실패: bucket={}, reason={}", bucket, exception.getClass().getSimpleName());
		}
	}

	private <T> T call(String action, Supplier<T> request) {
		try {
			T response = request.get();
			if (response == null) {
				throw new CustomException(StorageErrorCode.STORAGE_UNAVAILABLE);
			}
			return response;
		} catch (RestClientException exception) {
			log.warn("Storage 요청 실패: action={}, reason={}", action, exception.getClass().getSimpleName());
			throw new CustomException(StorageErrorCode.STORAGE_UNAVAILABLE);
		}
	}

	private void ensureConfigured() {
		if (!properties.configured()) {
			log.warn("Storage 설정이 비어 있어 요청을 처리할 수 없습니다.");
			throw new CustomException(StorageErrorCode.STORAGE_UNAVAILABLE);
		}
	}

	private String absoluteUrl(String relativeUrl) {
		return trimTrailingSlash(properties.url()) + STORAGE_PATH + relativeUrl;
	}

	private static String trimTrailingSlash(String url) {
		if (url == null) {
			return "";
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	private record SignedUploadResponse(String url) {
	}

	private record SignedDownloadResponse(String signedURL) {
	}

	private record StoredObject(String name) {
	}

}
