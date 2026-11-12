package com.deary.server.domain.report.controller;

import com.deary.server.domain.report.dto.request.ReportSaveRequest;
import com.deary.server.domain.report.dto.response.ReportCreateResponse;
import com.deary.server.domain.report.service.ReportService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "신고")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@Operation(summary = "신고", description = "일기(DIARY: 일기 ID), 사용자(USER: 사용자 ID), 교정(CORRECTION: 교정 ID)을 신고한다. 운영자가 24시간 안에 검토한다. 같은 대상을 다시 신고하면 기존 신고 ID를 돌려준다. 신고한 일기는 신고한 사람에게 보이지 않는다. 사용자를 신고해도 자동으로 차단되지는 않는다.")
	@ApiResponse(responseCode = "200", description = "신고 성공")
	@ApiResponse(responseCode = "400", description = "입력 오류 (COMMON_INVALID_INPUT, COMMON_INVALID_REQUEST_BODY), 나 자신이나 내 글 (REPORT_SELF)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "없는 대상 (REPORT_TARGET_NOT_FOUND), 탈퇴한 사용자 (USER_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "같은 요청이 동시에 처리됨 (COMMON_CONFLICT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/reports")
	public BaseResponse<ReportCreateResponse> createReport(
			@AuthenticationPrincipal AuthUser authUser,
			@Valid @RequestBody ReportSaveRequest request
	) {
		return BaseResponse.ok("신고를 접수했습니다.", reportService.registerReport(authUser.userId(), request));
	}

}
