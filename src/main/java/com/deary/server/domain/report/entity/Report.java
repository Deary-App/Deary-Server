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

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "reporter_id", nullable = false)
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

}
