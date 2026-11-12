package com.deary.server.domain.block.repository;

import com.deary.server.domain.block.entity.UserBlock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

	boolean existsUserBlockByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

	@Query("""
			select count(b) > 0 from UserBlock b
			where (b.blocker.id = :userId and b.blocked.id = :otherUserId)
			or (b.blocker.id = :otherUserId and b.blocked.id = :userId)
			""")
	boolean existsUserBlockBetween(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);

	@Query("""
			select b from UserBlock b
			where b.blocker.id = :blockerId
			order by b.createdAt desc, b.id desc
			""")
	List<UserBlock> findAllUserBlockByBlockerId(@Param("blockerId") Long blockerId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from UserBlock b where b.blocker.id = :blockerId and b.blocked.id = :blockedId")
	int deleteUserBlockByBlockerIdAndBlockedId(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);

}
