package com.deary.server.domain.diary.entity;

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
@Table(name = "diary_photos")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryPhoto extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "storage_path", nullable = false)
	private String storagePath;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "position_x", nullable = false)
	private double positionX;

	@Column(name = "position_y", nullable = false)
	private double positionY;

	@Column(name = "scale", nullable = false)
	private double scale;

	@Column(name = "rotation", nullable = false)
	private double rotation;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "diary_id", nullable = false)
	private Diary diary;

	private DiaryPhoto(Diary diary, String storagePath, int sortOrder, Layout layout) {
		this.diary = diary;
		this.storagePath = storagePath;
		this.sortOrder = sortOrder;
		this.positionX = layout.positionX();
		this.positionY = layout.positionY();
		this.scale = layout.scale();
		this.rotation = layout.rotation();
	}

	static DiaryPhoto create(Diary diary, String storagePath, int sortOrder, Layout layout) {
		return new DiaryPhoto(diary, storagePath, sortOrder, layout);
	}

	public record Layout(
			double positionX,
			double positionY,
			double scale,
			double rotation
	) {
	}

}
