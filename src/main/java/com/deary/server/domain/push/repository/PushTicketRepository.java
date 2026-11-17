package com.deary.server.domain.push.repository;

import com.deary.server.domain.push.entity.PushTicket;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushTicketRepository extends JpaRepository<PushTicket, Long> {

	@Query("""
			select t from PushTicket t
			where t.createdAt <= :createdBefore and t.id > :lastId
			order by t.id
			""")
	List<PushTicket> findAllPushTicketByCreatedAtBeforeAndIdGreaterThan(@Param("createdBefore") Instant createdBefore,
			@Param("lastId") long lastId, Limit limit);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PushTicket t where t.id in :ids")
	int deleteAllPushTicketByIdIn(@Param("ids") Collection<Long> ids);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PushTicket t where t.createdAt < :createdBefore")
	int deleteAllPushTicketByCreatedAtBefore(@Param("createdBefore") Instant createdBefore);

}
