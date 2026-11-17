package com.deary.server.domain.push.entity;

import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Expo에 보낸 푸시 한 건의 접수표. 나중에 전달 결과(receipt)를 조회할 때 쓴다.
 */
@Getter
@Entity
@Table(name = "push_tickets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushTicket extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "ticket_id", nullable = false, unique = true, length = 64)
	private String ticketId;

	@Column(name = "token", nullable = false, length = 255)
	private String token;

	private PushTicket(String ticketId, String token) {
		this.ticketId = ticketId;
		this.token = token;
	}

	public static PushTicket create(String ticketId, String token) {
		return new PushTicket(ticketId, token);
	}

}
