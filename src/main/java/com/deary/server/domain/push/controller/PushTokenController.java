package com.deary.server.domain.push.controller;

import com.deary.server.domain.push.dto.request.PushTokenRemoveRequest;
import com.deary.server.domain.push.dto.request.PushTokenSaveRequest;
import com.deary.server.domain.push.service.PushTokenService;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import com.deary.server.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Push", description = "푸시 알림 토큰")
@RestController
@RequestMapping("/api/v1/users/me/push-tokens")
@RequiredArgsConstructor
public class PushTokenController {

	private final PushTokenService pushTokenService;

	@Operation(summary = "푸시 토큰 등록",
			description = "이 기기의 Expo 푸시 토큰을 내 계정에 등록한다. 등록한 기기로 교정 도착·고마워요 알림을 보낸다. "
					+ "같은 토큰을 다시 보내면 주인과 언어만 지금 값으로 바꾼다.")
	@ApiResponse(responseCode = "200", description = "등록 성공")
	@ApiResponse(responseCode = "400", description = "토큰·언어 형식 오류 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "같은 요청이 동시에 처리됨 (COMMON_CONFLICT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PutMapping
	public BaseResponse<Void> updatePushToken(
			@AuthenticationPrincipal AuthUser authUser,
			@Valid @RequestBody PushTokenSaveRequest request
	) {
		pushTokenService.registerPushToken(authUser.userId(), request.token(), request.language());
		return BaseResponse.ok("푸시 토큰을 등록했습니다.");
	}

	@Operation(summary = "푸시 토큰 삭제", description = "로그아웃할 때 이 기기의 토큰을 지운다. 내 토큰이 아니거나 이미 없어도 성공으로 응답한다.")
	@ApiResponse(responseCode = "200", description = "삭제 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/remove")
	public BaseResponse<Void> deletePushToken(
			@AuthenticationPrincipal AuthUser authUser,
			@Valid @RequestBody PushTokenRemoveRequest request
	) {
		pushTokenService.removePushToken(authUser.userId(), request.token());
		return BaseResponse.ok("푸시 토큰을 삭제했습니다.");
	}

}
