package com.deary.server.domain.terms.controller;

import com.deary.server.domain.terms.dto.request.ConsentSaveRequest;
import com.deary.server.domain.terms.dto.response.ConsentFindAllResponse;
import com.deary.server.domain.terms.dto.response.TermsFindAllResponse;
import com.deary.server.domain.terms.dto.response.TermsFindResponse;
import com.deary.server.domain.terms.service.TermsService;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import com.deary.server.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Terms", description = "약관과 동의")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TermsController {

	private final TermsService termsService;

	@Operation(summary = "현재 약관 목록", description = "종류별 최신 버전 약관의 제목·버전·필수 여부를 조회한다.")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/terms")
	public BaseResponse<TermsFindAllResponse> readAllTerms() {
		return BaseResponse.ok("약관 목록을 조회했습니다.", termsService.findAllCurrentTerms());
	}

	@Operation(summary = "약관 본문", description = "약관 한 건의 본문을 조회한다.")
	@SecurityRequirements
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "없는 약관 (TERMS_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/terms/{termsId}")
	public BaseResponse<TermsFindResponse> readTerms(@PathVariable Long termsId) {
		return BaseResponse.ok("약관을 조회했습니다.", termsService.findTerms(termsId));
	}

	@Operation(summary = "약관 동의 저장", description = "현재 약관에 대한 동의를 이력으로 저장한다. 필수 약관은 철회할 수 없고, 저장 후 필수 약관에 모두 동의한 상태여야 한다.")
	@ApiResponse(responseCode = "200", description = "저장 성공. 저장 후 내 동의 상태를 반환")
	@ApiResponse(responseCode = "400", description = "필수 미동의·옛 버전·중복 약관 (TERMS_REQUIRED_NOT_AGREED, TERMS_NOT_CURRENT, TERMS_DUPLICATE), 입력 오류 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "탈퇴한 사용자 (USER_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/users/me/consents")
	public BaseResponse<ConsentFindAllResponse> createConsents(
			@AuthenticationPrincipal AuthUser authUser,
			@Valid @RequestBody ConsentSaveRequest request
	) {
		return BaseResponse.ok("약관 동의를 저장했습니다.", termsService.registerConsents(authUser.userId(), request));
	}

	@Operation(summary = "내 약관 동의 상태", description = "현재 약관마다 내 최신 동의 여부와 시각, 필수 약관 동의 완료 여부를 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/users/me/consents")
	public BaseResponse<ConsentFindAllResponse> readAllConsents(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("약관 동의 상태를 조회했습니다.", termsService.findAllConsent(authUser.userId()));
	}

}
