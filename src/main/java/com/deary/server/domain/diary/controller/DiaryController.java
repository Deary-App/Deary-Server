package com.deary.server.domain.diary.controller;

import com.deary.server.domain.diary.dto.request.DiarySaveRequest;
import com.deary.server.domain.diary.dto.response.DiaryCreateResponse;
import com.deary.server.domain.diary.dto.response.DiaryFindAllByMonthResponse;
import com.deary.server.domain.diary.dto.response.DiaryFindResponse;
import com.deary.server.domain.diary.dto.response.DiaryTodayFindResponse;
import com.deary.server.domain.diary.service.DiaryService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Diary", description = "내 기록")
@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
public class DiaryController {

	private final DiaryService diaryService;

	@Operation(summary = "새 기록 작성", description = "한국시간 오늘 날짜로 일기를 저장하고 공개하며 사람 교정 요청을 만든다. 사진 경로는 일기 사진 업로드 URL 발급으로 받은 path를 쓴다.")
	@ApiResponse(responseCode = "201", description = "작성 성공. 일기 ID와 첫 사진 URL 반환")
	@ApiResponse(responseCode = "400", description = "입력 오류 (COMMON_INVALID_INPUT), 사진 경로 오류 (DIARY_INVALID_PHOTO), 문장이 없는 본문 (DIARY_EMPTY_CONTENT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "회원가입 정보 없음 (PROFILE_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "오늘 이미 작성함 (DIARY_ALREADY_WRITTEN_TODAY)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping
	public ResponseEntity<BaseResponse<DiaryCreateResponse>> createDiary(
			@AuthenticationPrincipal AuthUser authUser,
			@Valid @RequestBody DiarySaveRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.of(
				HttpStatus.CREATED, "기록을 저장했습니다.", diaryService.generateDiary(authUser.userId(), request)));
	}

	@Operation(summary = "오늘 기록 여부", description = "한국시간 오늘 이미 쓴 기록이 있는지와 그 ID를 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@GetMapping("/today")
	public BaseResponse<DiaryTodayFindResponse> readTodayDiary(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("오늘 기록 여부를 조회했습니다.", diaryService.findTodayDiary(authUser.userId()));
	}

	@Operation(summary = "월별 내 기록", description = "그 달의 내 기록을 날짜순으로 조회한다. 첫 사진 URL, 교정 진행 상태와 별 표시 상태(correctionBadge: NOT_CORRECTED 회색 별, UNREAD 빨간 별, NONE 별 없음)를 포함한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "400", description = "잘못된 연도·월 (DIARY_INVALID_MONTH, COMMON_INVALID_INPUT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping
	public BaseResponse<DiaryFindAllByMonthResponse> readAllDiaryByMonth(
			@AuthenticationPrincipal AuthUser authUser,
			@RequestParam int year,
			@RequestParam int month
	) {
		return BaseResponse.ok("기록을 조회했습니다.", diaryService.findAllDiaryByMonth(authUser.userId(), year, month));
	}

	@Operation(summary = "내 기록 상세", description = "내 기록의 날짜·제목·본문·사진(배치 포함), 교정 진행 상태와 별 표시 상태(correctionBadge)를 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "404", description = "없거나 내 기록이 아님 (DIARY_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/{diaryId}")
	public BaseResponse<DiaryFindResponse> readDiary(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		return BaseResponse.ok("기록을 조회했습니다.", diaryService.findMyDiary(authUser.userId(), diaryId));
	}

	@Operation(summary = "내 기록 삭제", description = "기록과 사진, 교정 요청·결과를 삭제한다. 같은 날 다시 쓸 수 있다.")
	@ApiResponse(responseCode = "200", description = "삭제 성공")
	@ApiResponse(responseCode = "404", description = "없거나 내 기록이 아님 (DIARY_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@DeleteMapping("/{diaryId}")
	public BaseResponse<Void> deleteDiary(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long diaryId
	) {
		diaryService.removeDiary(authUser.userId(), diaryId);
		return BaseResponse.ok("기록을 삭제했습니다.");
	}

}
