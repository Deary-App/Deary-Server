package com.deary.server.domain.correction.repository;

import com.deary.server.domain.correction.entity.Correction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CorrectionRepository extends JpaRepository<Correction, Long> {

	Optional<Correction> findCorrectionByRequestDiaryId(Long diaryId);

	boolean existsCorrectionByRequestDiaryIdAndCorrectorId(Long diaryId, Long correctorId);

	@Query("""
			select count(c) > 0 from Correction c
			where c.request.diary.user.id = :userId and c.readAt is null
			""")
	boolean existsUnreadCorrectionByDiaryUserId(@Param("userId") Long userId);

	@Query("""
			select c from Correction c
			join fetch c.request r
			join fetch r.diary d
			where c.corrector.id = :correctorId
			and (:cursor is null or c.id < :cursor)
			order by c.id desc
			""")
	List<Correction> findAllCorrectionByCorrectorId(@Param("correctorId") Long correctorId,
			@Param("cursor") Long cursor, Limit limit);

	@Query("select c.request.id from Correction c where c.corrector.id = :correctorId")
	List<Long> findAllRequestIdByCorrectorId(@Param("correctorId") Long correctorId);

}
