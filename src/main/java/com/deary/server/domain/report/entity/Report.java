package com.deary.server.domain.report.entity;

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

@Getter
@Entity
@Table(name = "reports")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "target_type", nullable = false, length = 20)
	private ReportTargetType targetType;

	@Column(name = "target_id", nullable = false)
	private Long targetId;

	@Enumerated(EnumType.STRING)
	@Column(name = "reason", nullable = false, length = 20)
	private ReportReason reason;

	@Column(name = "detail", length = 500)
	private String detail;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private ReportStatus status = ReportStatus.PENDING;

	@Column(name = "handled_at")
	private Instant handledAt;

	@Column(name = "memo", length = 500)
	private String memo;

	@Enumerated(EnumType.STRING)
	@Column(name = "source", nullable = false, length = 10)
	private ReportSource source = ReportSource.USER;

	// AI 자동 검수 신고는 신고한 사람이 없다
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reporter_id")
	private User reporter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "target_user_id")
	private User targetUser;

	private Report(User reporter, ReportTargetType targetType, Long targetId, User targetUser,
			ReportReason reason, String detail) {
		this.reporter = reporter;
		this.targetType = targetType;
		this.targetId = targetId;
		this.targetUser = targetUser;
		this.reason = reason;
		this.detail = detail;
	}

	public static Report create(User reporter, ReportTargetType targetType, Long targetId, User targetUser,
			ReportReason reason, String detail) {
		return new Report(reporter, targetType, targetId, targetUser, reason, detail);
	}

	public static Report createAuto(ReportTargetType targetType, Long targetId, User targetUser,
			ReportReason reason, String detail) {
		Report report = new Report(null, targetType, targetId, targetUser, reason, detail);
		report.source = ReportSource.AUTO;
		report.status = ReportStatus.AUTO_HIDDEN;
		return report;
	}

	public boolean isAuto() {
		return source == ReportSource.AUTO;
	}

	/**
	 * 같은 대상이 자동 검수에 다시 걸렸다. 운영자 확인을 다시 기다린다.
	 */
	public void reflag(ReportReason reason, String detail) {
		this.reason = reason;
		this.detail = detail;
		this.status = ReportStatus.AUTO_HIDDEN;
		this.memo = null;
		this.handledAt = null;
	}

	public void handle(ReportStatus status, String memo, Instant now) {
		this.status = status;
		this.memo = memo;
		this.handledAt = status == ReportStatus.PENDING || status == ReportStatus.AUTO_HIDDEN ? null : now;
	}

}
