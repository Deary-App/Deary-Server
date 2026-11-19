package com.deary.server.domain.correction.entity;

import com.deary.server.domain.diary.entity.DiarySentence;
import com.deary.server.domain.user.entity.User;
import com.deary.server.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "corrections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Correction extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "comment", nullable = false, length = 1000)
	private String comment;

	@Column(name = "read_at")
	private Instant readAt;

	@Column(name = "hidden_at")
	private Instant hiddenAt;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "request_id", nullable = false, unique = true)
	private CorrectionRequest request;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "corrector_id", nullable = false)
	private User corrector;

	@OneToMany(mappedBy = "correction", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CorrectionItem> items = new ArrayList<>();

	private Correction(CorrectionRequest request, User corrector, String comment) {
		this.request = request;
		this.corrector = corrector;
		this.comment = comment;
	}

	public static Correction create(CorrectionRequest request, User corrector, String comment) {
		return new Correction(request, corrector, comment);
	}

	public void addItem(DiarySentence sentence, boolean changed,
			String correctedContent, String reason) {
		items.add(CorrectionItem.create(this, sentence, changed, correctedContent, reason));
	}

	public boolean isHidden() {
		return hiddenAt != null;
	}

	/**
	 * 운영자가 숨긴 교정과 정지된 사용자의 교정은 교정한 본인에게만 보인다.
	 */
	public boolean isVisibleTo(Long viewerId) {
		return corrector.getId().equals(viewerId) || (!isHidden() && !corrector.isSuspended());
	}

	public void hide(Instant now) {
		if (hiddenAt == null) {
			hiddenAt = now;
		}
	}

	public void unhide() {
		hiddenAt = null;
	}

	public void markRead(Instant now) {
		if (readAt == null) {
			readAt = now;
		}
	}

}
