package com.deary.server.domain.block.entity;

import com.deary.server.domain.user.entity.User;
import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_blocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBlock extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "blocker_id", nullable = false)
	private User blocker;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "blocked_id", nullable = false)
	private User blocked;

	private UserBlock(User blocker, User blocked) {
		this.blocker = blocker;
		this.blocked = blocked;
	}

	public static UserBlock create(User blocker, User blocked) {
		return new UserBlock(blocker, blocked);
	}

}
