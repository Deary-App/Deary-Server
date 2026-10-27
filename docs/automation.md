# Deary Server Git 작업 자동화 운영

`AGENTS.md`는 매 작업의 실행 순서와 권한 경계를 정하고, `docs/conventions/`는 주제별 팀 규칙을 담는다. `.agents/skills/git/`는 이슈·커밋·푸시·PR의 **별도 단계**를 맡는다. `CLAUDE.md`는 `@AGENTS.md`로 같은 안내를 가져오므로 Codex와 Claude가 같은 절차를 따른다. 새 대화에서는 현재 Git·GitHub 상태를 확인해 중단된 단계부터 이어가고, 작업과 관계없는 문서는 읽지 않는다.

## 작업별 실행 경계

| 단계 | 에이전트가 처리할 일 | 사람이 확인할 지점 |
| --- | --- | --- |
| 이슈·브랜치 준비 | 기존 항목을 재사용하고 필요할 때만 이슈 생성과 연결 브랜치 checkout | 구현으로 이어갈지 결정 |
| 구현·검증 | 관련 코드·규칙 확인, 필요한 변경과 검증 수행 | 변경 내용과 검증 결과 확인 |
| 커밋 계획 | 비밀정보 검사, 작은 커밋별 파일·메시지 제시 | 계획 수정, 로컬 커밋 또는 푸시 선택 |
| 커밋 실행 | 승인 뒤 변경이 없으면 계획한 단위만 로컬 커밋 | 커밋만 요청했다면 푸시 여부 결정 |
| 푸시 | 직전 계획이 있으면 승인된 커밋을 만든 뒤, 없으면 기존 로컬 커밋만 현재 작업 브랜치에 일반 푸시 | PR 제출 여부 결정 |
| PR | 기존 PR 재사용 또는 `develop` 대상 PR 생성·갱신, CI 상태 확인 | 리뷰·머지 판단, 서버 톡방 알림 |
| 배포 | `deploy` 대상 PR과 CI 상태 확인 | 운영 반영 결정과 `deploy` 머지 |

각 단계는 실제 상태를 조회한 뒤 한 번만 수행한다. 기존 이슈·연결 브랜치·열린 PR이 있으면 중복 생성하지 않는다. 한 요청에 여러 단계가 포함되어도 가장 앞의 미완료 단계만 수행한다. 다만 커밋 계획을 확인한 다음 요청의 "푸시해줘"는 계획 승인·커밋·푸시를 하나의 연속 단계로 처리한다.

단계별 트리거와 실행 방법은 [deary-issue](../.agents/skills/git/deary-issue/SKILL.md), [deary-commit](../.agents/skills/git/deary-commit/SKILL.md), [deary-push](../.agents/skills/git/deary-push/SKILL.md), [deary-pr](../.agents/skills/git/deary-pr/SKILL.md)를 원본으로 삼는다. 이슈·브랜치·커밋·PR의 이름과 형식은 [git-convention.md](conventions/git-convention.md)에만 정의한다.

## GitHub 자동화

| 파일 | 동작 |
| --- | --- |
| `.github/workflows/auto-label.yml` | 이슈·PR 제목의 `[prefix]` 또는 `prefix:`·`prefix(Class):`로 작업 유형 라벨을, 작성자 GitHub 아이디로 팀원 라벨을 붙인다. 제목이 바뀌면 관리 대상 라벨을 다시 맞춘다. |
| `.github/workflows/pr-check.yml` | `develop`·`deploy` 대상 PR에서 JDK 21로 `./gradlew build`를 실행한다. Testcontainers 테스트가 GitHub 러너의 Docker에서 함께 돈다. |
| `.coderabbit.yaml` | `develop`·`deploy` 대상의 Draft가 아닌 PR을 한국어로 자동 리뷰한다. Markdown과 `docs/`는 리뷰 대상에서 제외한다. |

- 팀원이 추가되면 `auto-label.yml`의 `authorLabels`와 저장소 라벨을 함께 추가한다.
- 로컬에만 있는 워크플로 파일을 실제 CI가 실행된 것으로 보고하지 않는다. 로컬 검증 결과와 CI 결과를 PR 본문에 구분해 적는다.
- 구현·로컬 검증이 끝난 PR 제출 요청은 리뷰 가능한 상태로 만들고, 미완료 작업 공유나 사용자의 Draft 요청일 때만 Draft를 사용한다.
- CI 실패나 리뷰 수정 요청은 PR 단계에서 자동으로 코드 수정·커밋·푸시하지 않고 다음 구현 요청으로 다룬다.
- 운영 배포 자동화는 아직 없다. 배포 워크플로를 추가하면 `deploy` 머지를 트리거로 하고 이 문서를 갱신한다.

## 사람이 설정해야 하는 것

- `Deary-App` org에 CodeRabbit GitHub App을 설치하고 이 저장소 접근을 허용한다.
- `develop`·`deploy` Ruleset에 PR 필수, `PR checks / build` 필수, 승인 리뷰, 대화 해결을 설정한다.
- 저장소 설정의 Pull Requests에서 머지 커밋 메시지를 `[merge] #<이슈번호> <PR 제목>` 형식으로 쓰도록 팀에서 맞춘다.
