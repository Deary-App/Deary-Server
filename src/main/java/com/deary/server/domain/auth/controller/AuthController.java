package com.deary.server.domain.auth.controller;

import com.deary.server.domain.auth.dto.request.AppleLoginRequest;
import com.deary.server.domain.auth.dto.request.GoogleLoginRequest;
import com.deary.server.domain.auth.dto.request.LogoutRequest;
import com.deary.server.domain.auth.dto.request.TokenReissueRequest;
import com.deary.server.domain.auth.dto.response.TokenIssueResponse;
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

	@Operation(summary = "Apple 로그인", description = "Apple identity token과 nonce 원문을 검증하고 서버 토큰을 발급한다. 처음이면 계정을 만든다. authorizationCode가 있으면 탈퇴 시 Apple 연결 해지에 쓸 refresh token을 받아 암호화해 저장한다(실패해도 로그인은 성공).")
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

	@Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 새 액세스·리프레시 토큰을 발급한다. 사용한 리프레시 토큰은 폐기된다.")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "재발급 성공")
	@ApiResponse(responseCode = "400", description = "필수 값 누락 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "없거나 만료되었거나 이미 사용한 리프레시 토큰 (AUTH_INVALID_REFRESH_TOKEN)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/reissue")
	public BaseResponse<TokenIssueResponse> createReissue(@Valid @RequestBody TokenReissueRequest request) {
		return BaseResponse.ok("토큰을 재발급했습니다.", authService.handleReissue(request));
	}

	@Operation(summary = "로그아웃", description = "이 기기의 리프레시 토큰을 폐기한다. 없는 토큰이어도 성공으로 응답한다.")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "로그아웃 성공")
	@ApiResponse(responseCode = "400", description = "필수 값 누락 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/logout")
	public BaseResponse<Void> processLogout(@Valid @RequestBody LogoutRequest request) {
		authService.handleLogout(request);
		return BaseResponse.ok("로그아웃했습니다.");
	}

}
