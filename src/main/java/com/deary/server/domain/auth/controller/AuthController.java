package com.deary.server.domain.auth.controller;

import com.deary.server.domain.auth.dto.request.AppleLoginRequest;
import com.deary.server.domain.auth.dto.request.GoogleLoginRequest;
import com.deary.server.domain.auth.dto.response.SocialLoginResponse;
import com.deary.server.domain.auth.service.AuthService;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "소셜 로그인과 토큰")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "Apple 로그인", description = "Apple identity token과 nonce 원문을 검증하고 서버 토큰을 발급한다. 처음이면 계정을 만든다.")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "로그인 성공")
	@ApiResponse(responseCode = "400", description = "필수 값 누락 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "유효하지 않은 Apple 토큰 또는 nonce 불일치 (AUTH_INVALID_SOCIAL_TOKEN)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "503", description = "Apple 공개키 조회 실패 (AUTH_SOCIAL_PROVIDER_UNAVAILABLE)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/apple")
	public BaseResponse<SocialLoginResponse> createAppleLogin(@Valid @RequestBody AppleLoginRequest request) {
		return BaseResponse.ok("로그인에 성공했습니다.", authService.handleAppleLogin(request));
	}

	@Operation(summary = "Google 로그인", description = "Google ID 토큰을 검증하고 서버 토큰을 발급한다. 처음이면 계정을 만든다.")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "로그인 성공")
	@ApiResponse(responseCode = "400", description = "필수 값 누락 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "유효하지 않은 Google 토큰 (AUTH_INVALID_SOCIAL_TOKEN)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "503", description = "Google 공개키 조회 실패 (AUTH_SOCIAL_PROVIDER_UNAVAILABLE)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/google")
	public BaseResponse<SocialLoginResponse> createGoogleLogin(@Valid @RequestBody GoogleLoginRequest request) {
		return BaseResponse.ok("로그인에 성공했습니다.", authService.handleGoogleLogin(request));
	}

}
