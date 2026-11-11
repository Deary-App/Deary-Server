package com.deary.server.domain.correction.repository;

import java.time.Instant;

public interface CorrectionReadState {

	Long getDiaryId();

	Instant getReadAt();

}
