package com.deary.server.domain.correction.entity;

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
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 교정을 받은 일기 작성자가 교정자에게 보내는 반응. 같은 교정에 같은 사람이 같은 종류는 한 번만 보낸다.
 * readAt은 교정자가 확인한 시각이다.
 */
@Getter
@Entity
@Table(name = "correction_reactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrectionReaction extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private CorrectionReactionType type;

	@Column(name = "read_at")
	private Instant readAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "correction_id", nullable = false)
	private Correction correction;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;

	private CorrectionReaction(Correction correction, User sender, CorrectionReactionType type) {
		this.correction = correction;
		this.sender = sender;
		this.type = type;
	}

	public static CorrectionReaction create(Correction correction, User sender, CorrectionReactionType type) {
		return new CorrectionReaction(correction, sender, type);
	}

}
