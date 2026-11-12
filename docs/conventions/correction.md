# 사람 교정

교정 요청·선점·제출·결과를 바꿀 때 읽는다.

## 상태

| `correction_requests.status` | 뜻 | 화면 표시 (`CorrectionProgress`) |
| --- | --- | --- |
| `OPEN` | 누구나 맡을 수 있음 | `WAITING` (교정 대기 중) |
| `CLAIMED` + `claim_expires_at` 이후 | 맡은 시간이 지남 | `WAITING` |
| `CLAIMED` + 맡은 시간 안 | 한 사람이 교정 중 | `IN_PROGRESS` (교정 중) |
| `COMPLETED` | 교정 1건 완료 | `COMPLETED` |

- 일기를 쓰면 요청이 `OPEN`으로 함께 만들어진다. 일기 하나에 요청 하나, 요청 하나에 교정 하나다(unique 제약).
- 선점 시간은 `CORRECTION_CLAIM_DURATION`(기본 30분)이다. 같은 사람이 다시 맡으면 시간을 다시 늘린다.
- 선점·완료 동시 요청은 `correction_requests.version` 낙관적 잠금과 `corrections.request_id` unique 제약으로 한 건만 성공한다.

## 규칙

- 내 글은 맡을 수 없고(`CORRECTION_OWN_DIARY`), 완료된 요청도 맡을 수 없다.
- 차단 관계(양방향)인 작성자의 일기나 내가 신고한 일기는 맡거나 제출할 수 없고 `DIARY_NOT_FOUND`로 응답한다.
- 제출은 지금 맡고 있는 사람만, 맡은 시간 안에 할 수 있다. 모든 문장에 정확히 한 번씩 응답해야 하고, `changed=true`면 고친 문장이 필요하다. 이유는 선택이다.
- 결과는 일기 작성자만 본다. 결과 화면 진입 시 읽음 처리 API를 호출하고, 홈의 빨간 점은 읽지 않은 교정이 있는지로 정한다.
- 교정자가 탈퇴하면 그 교정은 삭제되고 요청은 `OPEN`으로 돌아간다. 탈퇴자가 맡고 있던 요청도 풀린다. 이 처리는 `WithdrawalService`에서 사용자 삭제 전에 한다.
- 일기를 삭제하면 요청·교정·문장별 교정이 cascade로 함께 삭제된다.
- 교정 보상(우표)은 MVP에 없다.
