package com.deary.server.domain.auth.repository;

import com.deary.server.domain.auth.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findRefreshTokenByTokenHash(String tokenHash);

}
