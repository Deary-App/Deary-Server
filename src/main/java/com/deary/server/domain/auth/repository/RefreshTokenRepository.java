package com.deary.server.domain.auth.repository;

import com.deary.server.domain.auth.entity.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findRefreshTokenByTokenHash(String tokenHash);

	boolean existsByIdAndUser_IdAndExpiresAtAfter(Long id, Long userId, Instant now);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			update RefreshToken r
			set r.tokenHash = :newTokenHash, r.expiresAt = :expiresAt, r.updatedAt = :now
			where r.id = :id and r.tokenHash = :oldTokenHash
			""")
	int updateRefreshTokenHash(
			@Param("id") Long id,
			@Param("oldTokenHash") String oldTokenHash,
			@Param("newTokenHash") String newTokenHash,
			@Param("expiresAt") Instant expiresAt,
			@Param("now") Instant now
	);

	// 조회 뒤 다른 요청이 같은 세션을 재발급했으면(해시·만료가 바뀜) 그 세션은 지우지 않는다.
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			delete from RefreshToken r
			where r.id = :id and r.tokenHash = :tokenHash and r.expiresAt <= :now
			""")
	int deleteExpiredRefreshToken(@Param("id") Long id, @Param("tokenHash") String tokenHash, @Param("now") Instant now);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
	int deleteRefreshTokenByTokenHash(@Param("tokenHash") String tokenHash);

}
