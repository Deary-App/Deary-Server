package com.deary.server.domain.push.entity;

import com.deary.server.domain.user.entity.User;
import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 기기 하나의 Expo 푸시 토큰. 토큰은 기기마다 하나라서 다른 계정으로 로그인하면 주인을 바꾼다.
 */
@Getter
@Entity
@Table(name = "push_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "token", nullable = false, unique = true, length = 255)
	private String token;

	@Enumerated(EnumType.STRING)
	@Column(name = "language", nullable = false, length = 2)
	private PushLanguage language;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private PushToken(User user, String token, PushLanguage language) {
		this.user = user;
		this.token = token;
		this.language = language;
	}

	public static PushToken create(User user, String token, PushLanguage language) {
		return new PushToken(user, token, language);
	}

}
