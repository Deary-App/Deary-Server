package com.deary.server.domain.push.repository;

import com.deary.server.domain.push.entity.PushLanguage;
import com.deary.server.domain.push.entity.PushToken;
import com.deary.server.domain.user.entity.User;
import java.time.Instant;
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

	// 같은 값으로 다시 등록해도 updatedAt을 지금으로 갱신해, 등록 전에 보낸 푸시의 전달 결과로 지워지지 않게 한다.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			update PushToken p set p.user = :user, p.language = :language, p.updatedAt = :now
			where p.token = :token
			""")
	int updatePushTokenByToken(@Param("token") String token, @Param("user") User user,
			@Param("language") PushLanguage language, @Param("now") Instant now);

	// 이 시각 이후에 다시 등록된 토큰은 남긴다.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from PushToken p where p.token = :token and p.updatedAt <= :registeredBefore")
	int deletePushTokenByTokenAndUpdatedAtBefore(@Param("token") String token,
			@Param("registeredBefore") Instant registeredBefore);

}
