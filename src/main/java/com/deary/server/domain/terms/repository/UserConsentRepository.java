package com.deary.server.domain.terms.repository;

import com.deary.server.domain.terms.entity.UserConsent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

	@Query("""
			select c from UserConsent c
			where c.user.id = :userId
			and c.id in (
				select max(latest.id) from UserConsent latest
				where latest.user.id = :userId
				group by latest.terms.id
			)
			""")
	List<UserConsent> findAllLatestUserConsentByUserId(@Param("userId") Long userId);

}
