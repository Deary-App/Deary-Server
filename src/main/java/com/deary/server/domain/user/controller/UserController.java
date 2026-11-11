package com.deary.server.domain.user.controller;

import com.deary.server.domain.user.service.WithdrawalService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "회원")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final WithdrawalService withdrawalService;

	@Operation(summary = "회원탈퇴", description = "계정, 프로필, 일기와 사진, 내가 남긴 교정을 모두 삭제한다. 내가 교정한 요청은 다시 대기 상태가 된다. 복구할 수 없다.")
	@ApiResponse(responseCode = "200", description = "탈퇴 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@DeleteMapping("/me")
	public BaseResponse<Void> deleteMe(@AuthenticationPrincipal AuthUser authUser) {
		withdrawalService.handleWithdrawal(authUser.userId());
		return BaseResponse.ok("회원탈퇴가 완료되었습니다.");
	}

}
