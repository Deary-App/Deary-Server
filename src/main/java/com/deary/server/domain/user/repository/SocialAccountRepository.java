package com.deary.server.domain.user.repository;

import com.deary.server.domain.user.entity.SocialAccount;
import com.deary.server.domain.user.entity.SocialProvider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

	Optional<SocialAccount> findSocialAccountByProviderAndProviderUserId(SocialProvider provider, String providerUserId);

}
