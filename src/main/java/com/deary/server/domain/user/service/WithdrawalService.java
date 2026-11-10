package com.deary.server.domain.user.service;

import com.deary.server.domain.correction.service.CorrectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

	private final CorrectionService correctionService;
	private final UserService userService;

	@Transactional
	public void handleWithdrawal(Long userId) {
		correctionService.handleCorrectorWithdrawal(userId);
		userService.removeUser(userId);
	}

}
