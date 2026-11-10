package com.deary.server.domain.correction.entity;

import com.deary.server.domain.diary.entity.DiarySentence;
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
@Table(name = "correction_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrectionItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "changed", nullable = false)
	private boolean changed;

	@Column(name = "corrected_content", length = 2000)
	private String correctedContent;

	@Column(name = "reason", length = 500)
	private String reason;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "correction_id", nullable = false)
	private Correction correction;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "sentence_id", nullable = false)
	private DiarySentence sentence;

	private CorrectionItem(Correction correction, DiarySentence sentence, boolean changed, String correctedContent,
			String reason) {
		this.correction = correction;
		this.sentence = sentence;
		this.changed = changed;
		this.correctedContent = correctedContent;
		this.reason = reason;
	}

	static CorrectionItem create(Correction correction, DiarySentence sentence, boolean changed,
			String correctedContent, String reason) {
		return new CorrectionItem(correction, sentence, changed, correctedContent, reason);
	}

}
