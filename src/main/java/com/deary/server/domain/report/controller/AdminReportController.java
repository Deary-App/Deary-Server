package com.deary.server.domain.report.controller;

import com.deary.server.domain.report.dto.request.ReportModifyRequest;
import com.deary.server.domain.report.dto.response.ReportFindAllResponse;
import com.deary.server.domain.report.dto.response.ReportFindResponse;
import com.deary.server.domain.report.entity.ReportStatus;
import com.deary.server.domain.report.service.ReportModerationService;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Report", description = "운영자 신고 처리. 앱 토큰 대신 X-Admin-Key 헤더로 인증한다")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@ApiResponse(responseCode = "401", description = "운영자 키가 없거나 틀림 (COMMON_UNAUTHORIZED)",
		content = @Content(schema = @Schema(implementation = ErrorDto.class)))
public class AdminReportController {

	private final ReportModerationService reportModerationService;

	@Operation(summary = "신고함", description = "상태별 신고를 최신순으로 보여 준다. 같은 대상에 들어온 신고는 한 줄로 묶는다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/reports")
	public BaseResponse<ReportFindAllResponse> readAllReport(
			@Parameter(description = "PENDING(기본), AUTO_HIDDEN(AI 자동 숨김), DISMISSED, HIDDEN, SUSPENDED")
			@RequestParam(defaultValue = "PENDING") ReportStatus status
	) {
		return BaseResponse.ok("신고함 조회에 성공했습니다.", reportModerationService.findAllReport(status));
	}

	@Operation(summary = "신고 상세", description = "신고 내용과 대상(일기·교정·사용자), 작성자의 누적 신고 수를 보여 준다. 신고한 사람은 내려주지 않는다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "없는 신고 (REPORT_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/reports/{reportId}")
	public BaseResponse<ReportFindResponse> readReport(@PathVariable Long reportId) {
		return BaseResponse.ok("신고 조회에 성공했습니다.", reportModerationService.findReport(reportId));
	}

	@Operation(summary = "신고 처리",
			description = "DISMISS(문제없음, 걸려 있던 숨김·정지 해제), HIDE(일기·교정 숨김, 사용자 신고는 프로필 사진 내림. AI 자동 숨김은 숨김 확정),"
					+ " SUSPEND(작성자 정지), REOPEN(숨김·정지 해제 후 대기로)."
					+ " 같은 대상의 신고 전체에 함께 기록한다.")
	@ApiResponse(responseCode = "200", description = "처리 성공")
	@ApiResponse(responseCode = "400", description = "입력 오류 (COMMON_INVALID_INPUT, COMMON_INVALID_REQUEST_BODY)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "없는 신고 (REPORT_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "대상이 이미 삭제됨 (REPORT_TARGET_GONE)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PatchMapping("/reports/{reportId}")
	public BaseResponse<ReportFindResponse> updateReport(
			@PathVariable Long reportId,
			@Valid @RequestBody ReportModifyRequest request
	) {
		return BaseResponse.ok("신고를 처리했습니다.", reportModerationService.modifyReport(reportId, request));
	}

}
