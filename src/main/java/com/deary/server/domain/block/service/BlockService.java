package com.deary.server.domain.block.service;

import com.deary.server.domain.block.dto.response.BlockFindAllResponse;
import com.deary.server.domain.block.dto.response.BlockModifyResponse;
import com.deary.server.domain.block.entity.UserBlock;
import com.deary.server.domain.block.exception.BlockErrorCode;
import com.deary.server.domain.block.repository.UserBlockRepository;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.service.UserService;
import com.deary.server.global.exception.CommonErrorCode;
import com.deary.server.global.exception.CustomException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlockService {

	private final UserBlockRepository userBlockRepository;
	private final UserService userService;
	private final ProfileService profileService;

	@Transactional
	public BlockModifyResponse registerBlock(Long userId, Long targetUserId) {
		validateNotSelf(userId, targetUserId);
		User blocker = userService.findUser(userId);
		Profile target = profileService.findProfile(targetUserId);
		if (userBlockRepository.existsUserBlockByBlockerIdAndBlockedId(userId, targetUserId)) {
			return new BlockModifyResponse(true);
		}
		try {
			userBlockRepository.saveAndFlush(UserBlock.create(blocker, target.getUser()));
		} catch (DataIntegrityViolationException exception) {
			throw new CustomException(CommonErrorCode.CONFLICT);
		}
		return new BlockModifyResponse(true);
	}

	@Transactional
	public BlockModifyResponse removeBlock(Long userId, Long targetUserId) {
		validateNotSelf(userId, targetUserId);
		userBlockRepository.deleteUserBlockByBlockerIdAndBlockedId(userId, targetUserId);
		return new BlockModifyResponse(false);
	}

	@Transactional(readOnly = true)
	public BlockFindAllResponse findAllMyBlock(Long userId) {
		List<UserBlock> blocks = userBlockRepository.findAllUserBlockByBlockerId(userId);
		Map<Long, Profile> profiles = profileService.findAllProfileById(
				blocks.stream().map(block -> block.getBlocked().getId()).toList());
		return new BlockFindAllResponse(blocks.stream()
				.map(block -> toBlockedUser(block, profiles.get(block.getBlocked().getId())))
				.toList());
	}

	@Transactional(readOnly = true)
	public boolean isBlockedBetween(Long userId, Long otherUserId) {
		return userBlockRepository.existsUserBlockBetween(userId, otherUserId);
	}

	private void validateNotSelf(Long userId, Long targetUserId) {
		if (userId.equals(targetUserId)) {
			throw new CustomException(BlockErrorCode.SELF_BLOCK);
		}
	}

	private BlockFindAllResponse.BlockedUser toBlockedUser(UserBlock block, Profile profile) {
		return new BlockFindAllResponse.BlockedUser(
				block.getBlocked().getId(),
				profile == null ? null : profile.getNickname(),
				profile == null ? null : profileService.profileImageUrl(profile),
				block.getCreatedAt()
		);
	}

}
