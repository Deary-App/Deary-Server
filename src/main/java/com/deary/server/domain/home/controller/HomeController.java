package com.deary.server.domain.home.controller;

import com.deary.server.domain.home.dto.response.HomeFindResponse;
import com.deary.server.domain.home.service.HomeService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈")
@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

	private final HomeService homeService;

	@Operation(summary = "홈", description = "내 닉네임·프로필 사진과, 교정이 끝났는데 아직 안 본 내 기록이 있는지(이전 기록 탭 빨간 점)를 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "회원가입 정보 없음 (PROFILE_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping
	public BaseResponse<HomeFindResponse> readHome(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("홈을 조회했습니다.", homeService.findHome(authUser.userId()));
	}

}
