package com.deary.server.domain.terms.repository;

import com.deary.server.domain.terms.entity.Terms;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TermsRepository extends JpaRepository<Terms, Long> {

	@Query("""
			select t from Terms t
			where t.id in (select max(latest.id) from Terms latest group by latest.type)
			order by t.id
			""")
	List<Terms> findAllCurrentTerms();

}
