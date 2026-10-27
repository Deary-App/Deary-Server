---
name: deary-pr
description: Deary Server에서 "PR 만들어줘", "Draft PR 올려줘", "PR 갱신해줘"처럼 PR 단계를 명시적으로 요청할 때만 사용한다. 커밋·푸시는 하지 않는다.
---

# Deary PR

## 트리거와 단계 경계

- **"PR 만들어줘", "PR 올려줘"**: 이미 푸시된 작업 브랜치에서 `develop` 대상 PR을 생성하거나 기존 PR을 갱신한다.
- **"Draft PR 올려줘"**: 위 조건을 확인한 뒤 Draft 상태로 제출한다.
- **"PR 갱신해줘"**: 같은 head 브랜치의 기존 PR 제목·본문을 실제 diff에 맞게 수정한다.
- **"PR 본문 초안 써줘"**: 로컬에서 제목·본문만 작성한다. GitHub PR은 생성·수정하지 않는다.
- 이 스킬은 `git add`, `git commit`, `git push`를 실행하지 않는다.

## 실행

1. [Git 규칙](../../../../docs/conventions/git-convention.md)과 `.github/PULL_REQUEST_TEMPLATE.md`를 확인한다. `git status --short --branch`, `origin/develop...HEAD`의 커밋·변경 파일, 이슈 번호, head/base를 확인한다. `develop`·`deploy`에서 작업 브랜치 PR을 제출하지 않는다.
2. 현재 로컬 커밋이 원격 작업 브랜치에 모두 반영됐는지 확인한다. 미커밋 변경, 미푸시 커밋, 원격 브랜치 부재가 있으면 PR 본문 초안과 필요한 **커밋 또는 푸시 명령**을 알려주고 멈춘다.
3. 제목은 `[prefix] #번호 - 한국어 설명`으로 하고, 템플릿의 Related issue·Work Description·Trouble Shooting·Related ScreenShot·Uncompleted Tasks·To Reviewers를 실제 내용으로 채운다. 해당 없는 항목은 `없음`으로 적는다. 로컬 검증과 CI 결과를 구분하고, 코드로 확인한 사실만 적는다. 본문에 AI 도구 서명을 넣지 않는다.
4. 같은 head 브랜치의 열린 PR을 먼저 조회한다. 있으면 제목·본문만 갱신하고, 없으면 `gh pr create --base develop --head <브랜치> --title ... --body-file ...`로 생성한다. 담당자는 작성자로 지정한다. 완료된 변경은 리뷰 가능한 PR로, Draft 요청이나 미완료 공유는 `--draft`로 제출한다.
5. PR URL과 CI 상태를 확인해 보고한다. CodeRabbit은 Draft가 아닌 PR만 자동 리뷰하고 Markdown 변경은 제외한다. CI 실패나 리뷰 수정 요청은 다음 구현 단계에서 다룬다. 리뷰·승인, 머지, `deploy` 배포, 서버 톡방 알림은 자동으로 진행하지 않는다.
