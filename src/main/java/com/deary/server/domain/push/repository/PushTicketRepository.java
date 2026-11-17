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

	@Query("select t from PushTicket t where t.createdAt <= :createdBefore order by t.id")
	List<PushTicket> findAllPushTicketByCreatedAtBefore(@Param("createdBefore") Instant createdBefore, Limit limit);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PushTicket t where t.id in :ids")
	int deleteAllPushTicketByIdIn(@Param("ids") Collection<Long> ids);

}
