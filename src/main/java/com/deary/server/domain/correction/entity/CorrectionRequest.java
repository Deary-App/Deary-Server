package com.deary.server.domain.correction.entity;

import com.deary.server.domain.diary.entity.Diary;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "correction_requests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrectionRequest extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private CorrectionRequestStatus status;

	@Column(name = "claim_expires_at")
	private Instant claimExpiresAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "diary_id", nullable = false, unique = true)
	private Diary diary;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "claimed_by")
	private User claimedBy;

	private CorrectionRequest(Diary diary) {
		this.diary = diary;
		this.status = CorrectionRequestStatus.OPEN;
	}

	public static CorrectionRequest open(Diary diary) {
		return new CorrectionRequest(diary);
	}

	public void claim(User corrector, Instant expiresAt) {
		this.status = CorrectionRequestStatus.CLAIMED;
		this.claimedBy = corrector;
		this.claimExpiresAt = expiresAt;
	}

	public void release() {
		this.status = CorrectionRequestStatus.OPEN;
		this.claimedBy = null;
		this.claimExpiresAt = null;
	}

	public void complete(Instant now) {
		this.status = CorrectionRequestStatus.COMPLETED;
		this.completedAt = now;
		this.claimedBy = null;
		this.claimExpiresAt = null;
	}

	public void reopen() {
		this.status = CorrectionRequestStatus.OPEN;
		this.completedAt = null;
		this.claimedBy = null;
		this.claimExpiresAt = null;
	}

	public boolean isClaimedActiveBy(Long userId, Instant now) {
		return isClaimActiveAt(now) && claimedBy.getId().equals(userId);
	}

	public CorrectionProgress progressAt(Instant now) {
		if (status == CorrectionRequestStatus.COMPLETED) {
			return CorrectionProgress.COMPLETED;
		}
		if (isClaimActiveAt(now)) {
			return CorrectionProgress.IN_PROGRESS;
		}
		return CorrectionProgress.WAITING;
	}

	public boolean isCorrectableBy(Long userId, Instant now) {
		if (status == CorrectionRequestStatus.COMPLETED) {
			return false;
		}
		return !isClaimActiveAt(now) || claimedBy.getId().equals(userId);
	}

	public boolean isClaimActiveAt(Instant now) {
		return status == CorrectionRequestStatus.CLAIMED && claimExpiresAt != null && claimExpiresAt.isAfter(now);
	}

}
