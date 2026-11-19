package com.deary.server.domain.user.entity;

import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "email", length = 320)
	private String email;

	@Column(name = "suspended_at")
	private Instant suspendedAt;

	private User(String email) {
		this.email = email;
	}

	public static User create(String email) {
		return new User(email);
	}

	public boolean isSuspended() {
		return suspendedAt != null;
	}

	public void suspend(Instant now) {
		if (suspendedAt == null) {
			suspendedAt = now;
		}
	}

	public void unsuspend() {
		suspendedAt = null;
	}

}
