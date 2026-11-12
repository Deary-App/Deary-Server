package com.deary.server.domain.user.repository;

import com.deary.server.domain.user.entity.SocialAccount;
import com.deary.server.domain.user.entity.SocialProvider;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

	Optional<SocialAccount> findSocialAccountByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

	Optional<SocialAccount> findSocialAccountByProviderAndUserId(SocialProvider provider, Long userId);

	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update SocialAccount socialAccount
			set socialAccount.appleRefreshToken = :appleRefreshToken,
				socialAccount.appleClientId = :appleClientId,
				socialAccount.updatedAt = :updatedAt
			where socialAccount.provider = com.deary.server.domain.user.entity.SocialProvider.APPLE
				and socialAccount.providerUserId = :providerUserId
			""")
	int updateAppleTokenByProviderUserId(
			@Param("providerUserId") String providerUserId,
			@Param("appleRefreshToken") String appleRefreshToken,
			@Param("appleClientId") String appleClientId,
			@Param("updatedAt") Instant updatedAt
	);

}
