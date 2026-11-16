package com.deary.server.domain.push.repository;

import com.deary.server.domain.push.entity.PushToken;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

	Optional<PushToken> findPushTokenByToken(String token);

	List<PushToken> findAllPushTokenByUserId(Long userId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PushToken p where p.token = :token and p.user.id = :userId")
	int deletePushTokenByTokenAndUserId(@Param("token") String token, @Param("userId") Long userId);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PushToken p where p.token in :tokens")
	int deleteAllPushTokenByTokenIn(@Param("tokens") Collection<String> tokens);

}
