package com.deary.server.domain.profile.controller;

import com.deary.server.domain.profile.dto.request.ProfileSaveRequest;
import com.deary.server.domain.profile.dto.response.ProfileFindResponse;
import com.deary.server.domain.profile.dto.response.PublicProfileFindResponse;
import com.deary.server.domain.profile.dto.response.RegistrationFindResponse;
import com.deary.server.domain.profile.service.ProfileService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "회원가입 정보와 프로필")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	@Operation(summary = "회원가입 정보 저장·수정", description = "처음이면 저장하고, 있으면 수정한다. 수정할 때는 조회한 version을 함께 보낸다. 필수 약관 동의 후에만 저장할 수 있다.")
	@ApiResponse(responseCode = "200", description = "저장 성공")
	@ApiResponse(responseCode = "400", description = "입력 오류 (COMMON_INVALID_INPUT, COMMON_INVALID_REQUEST_BODY), 만 14세 미만 (PROFILE_UNDER_MINIMUM_AGE), 같은 언어 (PROFILE_SAME_LANGUAGE), 잘못된 국적 (PROFILE_INVALID_NATIONALITY), 수정 시 version 누락 (PROFILE_VERSION_REQUIRED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "403", description = "필수 약관 미동의 (PROFILE_TERMS_NOT_AGREED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "다른 기기에서 먼저 수정함 (PROFILE_VERSION_CONFLICT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PutMapping("/me/profile")
	public BaseResponse<ProfileFindResponse> updateMyProfile(
			@AuthenticationPrincipal AuthUser authUser,
			@Valid @RequestBody ProfileSaveRequest request
	) {
		return BaseResponse.ok("회원가입 정보를 저장했습니다.", profileService.handleProfileSave(authUser.userId(), request));
	}

	@Operation(summary = "내 프로필", description = "내 회원가입 정보와 계정 이메일, 수정에 쓸 version을 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "회원가입 정보 없음 (PROFILE_NOT_FOUND), 탈퇴한 사용자 (USER_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/me/profile")
	public BaseResponse<ProfileFindResponse> readMyProfile(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("프로필을 조회했습니다.", profileService.findMyProfile(authUser.userId()));
	}

	@Operation(summary = "가입 진행 상태", description = "스플래시 분기용. 필수 약관 동의 여부와 회원가입 정보 입력 여부를 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "탈퇴한 사용자 (USER_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/me/registration")
	public BaseResponse<RegistrationFindResponse> readMyRegistration(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("가입 진행 상태를 조회했습니다.", profileService.findRegistration(authUser.userId()));
	}

	@Operation(summary = "사용자 프로필", description = "다른 사용자의 공개 프로필을 조회한다. 생년월일 원본과 이메일은 내보내지 않고, 성별은 공개한 경우에만 보여준다. 차단 관계(양방향)인 사용자는 없는 사용자와 같이 응답한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "없거나 차단 관계인 사용자 (PROFILE_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/{userId}/profile")
	public BaseResponse<PublicProfileFindResponse> readProfile(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long userId
	) {
		return BaseResponse.ok("프로필을 조회했습니다.", profileService.findPublicProfile(authUser.userId(), userId));
	}

}
