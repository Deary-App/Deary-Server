package com.deary.server.domain.report.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportModifyRequest(
		@NotNull(message = "처리 방법을 선택해 주세요.")
		Action action,
		@Size(max = 500, message = "메모는 500자 이하로 입력해 주세요.")
		String memo
) {

	public enum Action {
		/** 문제없음: 대상은 그대로 둔다 */
		DISMISS,
		/** 숨기기: 일기·교정은 다른 사람에게서 숨기고, 사용자 신고는 프로필 사진을 내린다 */
		HIDE,
		/** 작성자 정지: 앱 사용을 막고 그 사람의 글·교정을 다른 사람에게서 숨긴다 */
		SUSPEND,
		/** 되돌리기: 숨김·정지를 풀고 다시 대기 상태로 돌린다 */
		REOPEN
	}

}
