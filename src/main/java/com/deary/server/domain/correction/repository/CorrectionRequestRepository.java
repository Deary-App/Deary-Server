package com.deary.server.domain.correction.repository;

import com.deary.server.domain.correction.entity.CorrectionRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorrectionRequestRepository extends JpaRepository<CorrectionRequest, Long> {

	Optional<CorrectionRequest> findCorrectionRequestByDiaryId(Long diaryId);

	List<CorrectionRequest> findAllCorrectionRequestByDiaryIdIn(Collection<Long> diaryIds);

	List<CorrectionRequest> findAllCorrectionRequestByClaimedById(Long userId);

}
