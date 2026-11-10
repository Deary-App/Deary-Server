package com.deary.server.domain.upload.controller;

import com.deary.server.domain.upload.dto.response.UploadUrlIssueResponse;
import com.deary.server.domain.upload.service.UploadService;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import com.deary.server.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Upload", description = "사진 업로드 URL")
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

	private final UploadService uploadService;

	@Operation(summary = "프로필 사진 업로드 URL 발급", description = "앱이 사진을 Storage에 직접 올릴 임시 URL과 저장 경로를 발급한다. 업로드 후 회원가입 정보 저장 시 profileImagePath에 path를 보낸다.")
	@ApiResponse(responseCode = "200", description = "발급 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "503", description = "Storage 연결 실패 (STORAGE_UNAVAILABLE)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/profile-image")
	public BaseResponse<UploadUrlIssueResponse> createProfileImageUpload(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("업로드 URL을 발급했습니다.", uploadService.generateProfileImageUploadUrl(authUser.userId()));
	}

}
