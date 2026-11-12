package com.deary.server.domain.block.controller;

import com.deary.server.domain.block.dto.response.BlockFindAllResponse;
import com.deary.server.domain.block.dto.response.BlockModifyResponse;
import com.deary.server.domain.block.service.BlockService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Block", description = "사용자 차단")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class BlockController {

	private final BlockService blockService;

	@Operation(summary = "사용자 차단", description = "사용자를 차단한다. 차단하면 서로의 공개 기록·프로필이 보이지 않고 서로의 기록을 교정할 수 없다. 이미 차단했어도 성공으로 응답한다.")
	@ApiResponse(responseCode = "200", description = "차단 성공")
	@ApiResponse(responseCode = "400", description = "나 자신을 차단 (BLOCK_SELF)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "404", description = "없는 사용자 (PROFILE_NOT_FOUND), 탈퇴한 사용자 (USER_NOT_FOUND)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@ApiResponse(responseCode = "409", description = "같은 요청이 동시에 처리됨 (COMMON_CONFLICT)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@PostMapping("/{userId}/block")
	public BaseResponse<BlockModifyResponse> createBlock(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long userId
	) {
		return BaseResponse.ok("사용자를 차단했습니다.", blockService.registerBlock(authUser.userId(), userId));
	}

	@Operation(summary = "사용자 차단 해제", description = "차단을 해제한다. 차단하지 않은 사용자여도 성공으로 응답한다.")
	@ApiResponse(responseCode = "200", description = "해제 성공")
	@ApiResponse(responseCode = "400", description = "나 자신을 지정 (BLOCK_SELF)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@DeleteMapping("/{userId}/block")
	public BaseResponse<BlockModifyResponse> deleteBlock(
			@AuthenticationPrincipal AuthUser authUser,
			@PathVariable Long userId
	) {
		return BaseResponse.ok("차단을 해제했습니다.", blockService.removeBlock(authUser.userId(), userId));
	}

	@Operation(summary = "내 차단 목록", description = "내가 차단한 사용자를 최근에 차단한 순으로 조회한다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	@ApiResponse(responseCode = "401", description = "인증 실패 (COMMON_UNAUTHORIZED)",
			content = @Content(schema = @Schema(implementation = ErrorDto.class)))
	@GetMapping("/me/blocks")
	public BaseResponse<BlockFindAllResponse> readAllMyBlock(@AuthenticationPrincipal AuthUser authUser) {
		return BaseResponse.ok("차단 목록을 조회했습니다.", blockService.findAllMyBlock(authUser.userId()));
	}

}
