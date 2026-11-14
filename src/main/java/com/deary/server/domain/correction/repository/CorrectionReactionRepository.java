package com.deary.server.domain.correction.repository;

import com.deary.server.domain.correction.entity.CorrectionReaction;
import com.deary.server.domain.correction.entity.CorrectionReactionType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrectionReactionRepository extends JpaRepository<CorrectionReaction, Long> {

	Optional<CorrectionReaction> findCorrectionReactionByCorrectionIdAndSenderIdAndType(
			Long correctionId, Long senderId, CorrectionReactionType type);

	@Query("select r.type from CorrectionReaction r where r.correction.id = :correctionId and r.sender.id = :senderId")
	List<CorrectionReactionType> findAllTypeByCorrectionIdAndSenderId(@Param("correctionId") Long correctionId,
			@Param("senderId") Long senderId);

	@Query("select r from CorrectionReaction r where r.correction.id in :correctionIds")
	List<CorrectionReaction> findAllCorrectionReactionByCorrectionIdIn(@Param("correctionIds") Collection<Long> correctionIds);

	@Query("""
			select count(r) > 0 from CorrectionReaction r
			where r.correction.corrector.id = :correctorId and r.readAt is null
			""")
	boolean existsUnreadCorrectionReactionByCorrectorId(@Param("correctorId") Long correctorId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update CorrectionReaction r set r.readAt = :now, r.updatedAt = :now
			where r.readAt is null
			and r.correction.id in (select c.id from Correction c where c.corrector.id = :correctorId)
			""")
	int updateAllCorrectionReactionReadByCorrectorId(@Param("correctorId") Long correctorId, @Param("now") Instant now);

}
