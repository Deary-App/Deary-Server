package com.deary.server.domain.diary.controller;

import com.deary.server.domain.diary.dto.response.PublicDiaryFindAllResponse;
import com.deary.server.domain.diary.dto.response.PublicDiaryFindResponse;
import com.deary.server.domain.diary.service.PublicDiaryService;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.global.exception.dto.ErrorDto;
import com.deary.server.global.response.BaseResponse;
import com.deary.server.global.security.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PublicDiary", description = "탐색과 공개 기록")
@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PublicDiaryController {

	private final PublicDiaryService publicDiaryService;

	@Operation(summary = "탐색 목록", description = "공개 일기를 최신순으로 조회한다. 차단 관계(양방향)인 사용자의 일기와 내가 신고한 일기는 빠진다. language를 빼면 전체. 응답의 nextCursor를 다음 요청 cursor로 보낸다(null이면 마지막).")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 언어·크기 (COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/public-diaries")
	public BaseResponse<PublicDiaryFindAllResponse> readAllPublicDiary(
			@AuthenticationPrincipal AuthUser authUser,
			@Parameter(description = "ko, ja, en 중 하나. 없으면 전체") @RequestParam(required = false) Language language,
			@RequestParam(required = false) Long cursor,
			@RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		return BaseResponse.ok("공개 기록을 조회했습니다.", publicDiaryService.findAllPublicDiary(authUser.userId(), language, cursor, size));
	}

	@Operation(summary = "공개 기록 상세", description = "공개 일기의 본문·사진 전체·작성자·교정 진행 상태와 내가 지금 교정할 수 있는지를 조회한다. 교정이 완료됐으면 correction에 문장별 교정 결과·교정자·전체 코멘트가 들어가고, 아니면 null이다. 이 조회는 작성자의 읽음 처리를 하지 않는다. 차단 관계(양방향)인 작성자의 기록이나 내가 신고한 기록은 DIARY_NOT_FOUND로 응답하고, 차단 관계인 교정자의 교정이나 내가 신고한 교정은 correction이 null이다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "삭제됐거나 없는 기록, 차단 관계이거나 내가 신고한 기록 (DIARY_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/public-diaries/{diaryId}")
	public BaseResponse<PublicDiaryFindResponse> readPublicDiary(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		return BaseResponse.ok("공개 기록을 조회했습니다.", publicDiaryService.findPublicDiary(authUser.userId(), diaryId));
	}

	@Operation(summary = "사용자 공개 기록 목록", description = "한 사용자의 공개 일기를 최신순으로 조회한다. 내가 신고한 일기는 빠진다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "없거나 차단 관계인 사용자 (PROFILE_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/users/{userId}/public-diaries")
	public BaseResponse<PublicDiaryFindAllResponse> readAllPublicDiaryByUser(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long userId,
			@RequestParam(required = false) Long cursor,
			@RequestParam(defaultValue = "20") @Min(value = 1, message = "size는 1 이상이어야 합니다.") @Max(value = 50, message = "size는 50 이하여야 합니다.") int size
	) {
		return BaseResponse.ok("공개 기록을 조회했습니다.", publicDiaryService.findAllPublicDiaryByUser(authUser.userId(), userId, cursor, size));
	}

}
