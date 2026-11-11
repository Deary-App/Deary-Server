package com.deary.server.domain.auth.repository;

import com.deary.server.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findRefreshTokenByTokenHash(String tokenHash);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from RefreshToken r where r.id = :id")
	int deleteRefreshTokenById(@Param("id") Long id);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
	int deleteRefreshTokenByTokenHash(@Param("tokenHash") String tokenHash);

}
