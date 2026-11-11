package com.deary.server.domain.upload.dto.response;

import java.util.List;

public record DiaryImageUploadIssueResponse(
		List<UploadUrlIssueResponse> uploads
) {
}
