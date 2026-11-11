package com.deary.server.domain.correction.controller;

import com.deary.server.domain.correction.dto.request.CorrectionSaveRequest;
import com.deary.server.domain.correction.dto.response.CorrectionClaimResponse;
import com.deary.server.domain.correction.dto.response.CorrectionCreateResponse;
import com.deary.server.domain.correction.dto.response.CorrectionFindAllResponse;
import com.deary.server.domain.correction.dto.response.CorrectionFindResponse;
import com.deary.server.domain.correction.service.CorrectionService;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import com.deary.server.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Correction", description = "사람 교정")
@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CorrectionController {

	private final CorrectionService correctionService;

	@Operation(summary = "교정 맡기", description = "교정 작성 화면 진입 시 호출한다. 그 일기의 교정을 일정 시간(기본 30분) 맡고 문장 목록을 받는다. 이미 내가 맡은 경우 시간을 다시 늘린다.")
	@ApiResponse(responseCode = "200", description = "맡기 성공")
	@ApiResponse(responseCode = "403", description = "내 기록 (CORRECTION_OWN_DIARY)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "없는 기록 (DIARY_NOT_FOUND), 회원가입 정보 없음 (PROFILE_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "다른 사람이 맡음 (CORRECTION_ALREADY_CLAIMED), 이미 완료 (CORRECTION_ALREADY_COMPLETED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/public-diaries/{diaryId}/correction-claim")
	public BaseResponse<CorrectionClaimResponse> createCorrectionClaim(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		return BaseResponse.ok("교정을 맡았습니다.", correctionService.handleClaim(authUser.userId(), diaryId));
	}

	@Operation(summary = "교정 맡기 반납", description = "내가 맡고 있는 교정을 내려놓는다. 맡고 있지 않으면 아무 일도 하지 않는다.")
	@ApiResponse(responseCode = "200", description = "반납 성공")
	@ApiResponse(responseCode = "404", description = "없는 기록 (DIARY_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@DeleteMapping("/public-diaries/{diaryId}/correction-claim")
	public BaseResponse<Void> deleteCorrectionClaim(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		correctionService.removeClaim(authUser.userId(), diaryId);
		return BaseResponse.ok("교정을 내려놓았습니다.");
	}

	@Operation(summary = "교정 제출", description = "맡은 교정을 제출한다. 모든 문장에 응답(changed=true면 correctedContent 필수)하고 전체 코멘트를 남겨야 한다.")
	@ApiResponse(responseCode = "201", description = "제출 성공")
	@ApiResponse(responseCode = "400", description = "입력 오류 (COMMON_INVALID_INPUT), 문장 누락·중복 (CORRECTION_INCOMPLETE_ITEMS), 고친 문장 누락 (CORRECTION_INVALID_ITEM)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "맡은 시간이 지났거나 맡지 않음 (CORRECTION_CLAIM_NOT_ACTIVE), 이미 완료 (CORRECTION_ALREADY_COMPLETED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/public-diaries/{diaryId}/corrections")
	public ResponseEntity<BaseResponse<CorrectionCreateResponse>> createCorrection(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId,
			@Valid @RequestBody CorrectionSaveRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.of(HttpStatus.CREATED, "교정을 제출했습니다.",
				correctionService.generateCorrection(authUser.userId(), diaryId, request)));
	}

	@Operation(summary = "내 기록 교정 결과", description = "내 기록의 문장별 원문·교정문·이유와 교정자, 전체 코멘트를 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "없거나 내 기록이 아님 (DIARY_NOT_FOUND), 결과 없음 (CORRECTION_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/diaries/{diaryId}/correction")
	public BaseResponse<CorrectionFindResponse> readCorrection(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		return BaseResponse.ok("교정 결과를 조회했습니다.", correctionService.findCorrectionOfMyDiary(authUser.userId(), diaryId));
	}

	@Operation(summary = "교정 결과 읽음 처리", description = "교정 결과 화면 진입 시 호출한다. 홈의 빨간 점이 사라진다.")
	@ApiResponse(responseCode = "200", description = "처리 성공")
	@ApiResponse(responseCode = "404", description = "없거나 내 기록이 아님 (DIARY_NOT_FOUND), 결과 없음 (CORRECTION_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/diaries/{diaryId}/correction/read")
	public BaseResponse<Void> processCorrectionRead(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		correctionService.modifyCorrectionRead(authUser.userId(), diaryId);
		return BaseResponse.ok("읽음 처리했습니다.");
	}

	@Operation(summary = "내 첨삭 활동", description = "내가 다른 사람에게 해 준 교정을 최신순으로 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/users/me/corrections")
	public BaseResponse<CorrectionFindAllResponse> readAllMyCorrection(
			@AuthenticationPrincipal AuthUser authUser,
			@RequestParam(required = false) Long cursor,
			@RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		return BaseResponse.ok("첨삭 활동을 조회했습니다.", correctionService.findAllMyCorrection(authUser.userId(), cursor, size));
	}

}
