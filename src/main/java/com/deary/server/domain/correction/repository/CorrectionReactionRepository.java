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

	// 교정한 사람과 보낸 사람이 차단 관계(양방향)인 반응은 교정한 사람에게 보이지 않게 뺀다.
	@Query("""
			select r from CorrectionReaction r
			where r.correction.id in :correctionIds
			and not exists (
				select b.id from UserBlock b
				where b.blocker.id = r.correction.corrector.id and b.blocked.id = r.sender.id
			)
			and not exists (
				select b.id from UserBlock b
				where b.blocker.id = r.sender.id and b.blocked.id = r.correction.corrector.id
			)
			""")
	List<CorrectionReaction> findAllCorrectionReactionByCorrectionIdIn(@Param("correctionIds") Collection<Long> correctionIds);

	// 차단 관계(양방향)인 사람이 보낸 반응은 확인하지 않은 반응으로 세지 않는다.
	@Query("""
			select count(r) > 0 from CorrectionReaction r
			where r.correction.corrector.id = :correctorId and r.readAt is null
			and not exists (
				select b.id from UserBlock b
				where b.blocker.id = :correctorId and b.blocked.id = r.sender.id
			)
			and not exists (
				select b.id from UserBlock b
				where b.blocker.id = r.sender.id and b.blocked.id = :correctorId
			)
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
