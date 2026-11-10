package com.deary.server.domain.diary.dto.response;

import com.deary.server.domain.diary.entity.DiaryPhoto;

public record DiaryPhotoResponse(
		int sortOrder,
		String url,
		double positionX,
		double positionY,
		double scale,
		double rotation
) {

	public static DiaryPhotoResponse of(DiaryPhoto photo, String url) {
		return new DiaryPhotoResponse(
				photo.getSortOrder(), url, photo.getPositionX(), photo.getPositionY(), photo.getScale(),
				photo.getRotation());
	}

}
