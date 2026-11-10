package com.deary.server.domain.user.repository;

import com.deary.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("delete from User u where u.id = :id")
	int deleteUserById(@Param("id") Long id);

}
