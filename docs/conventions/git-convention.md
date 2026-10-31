# Git·이슈·PR 컨벤션

이슈, 브랜치, 커밋, PR 또는 배포 흐름을 다룰 때 읽는다. 기존 브랜치·커밋을 형식만 맞추려고 다시 쓰지 않는다.

## 작업 흐름

1. 이슈를 생성한다.
2. 이슈에 연결된 작업 브랜치를 `develop` 기준으로 생성한다.
3. add → commit → push → pull request를 진행한다.
4. 코드 리뷰를 받는다.
5. PR을 `develop`으로 머지한다. 배포할 때는 `deploy` 브랜치에도 머지한다.

- `develop`에서 직접 작업하는 것은 원칙적으로 금지한다. 단, README 작성과 GitHub 템플릿(이슈, PR) 작성은 `develop`에서 할 수 있다.
- commit, push, merge, pull request 등 모든 작업은 오류 없이 정상적으로 실행되는지 확인한 뒤 수행한다.
- `deploy`에는 직접 커밋·푸시하지 않는다.

단계별 실행 경계와 사람의 확인 지점은 [자동화 운영 문서](../automation.md), 실제 실행 절차는 이 문서 하단의 각 스킬을 따른다.

## Prefix

이슈·브랜치·커밋·PR은 같은 prefix를 쓴다.

| prefix | 의미 |
| --- | --- |
| `feat` | 새로운 기능 구현 |
| `fix` | 버그·로직 수정 |
| `refactor` | 내부 동작은 바꾸지 않고 기존 코드를 개선 |
| `chore` | 패키지 구조 수정, 의존성 추가, yml 수정, 파일 이동 등 코드와 무관한 작업 (`.gitignore`, `build.gradle` 등) |
| `docs` | 문서 추가·수정·삭제 |
| `style` | 코드 포맷팅, 세미콜론 누락 등 동작 변경이 없는 경우 |
| `remove` | 패키지나 클래스 삭제 |
| `test` | 테스트 코드 추가·수정 |
| `rename` | 패키지나 클래스명 수정 |
| `comment` | 주석 추가 |
| `hotfix` | 긴급 수정 |
| `init` | 프로젝트 초기 세팅 |

## 이슈

- 작업 시작 전에 이슈를 먼저 만든다. 이슈는 작업 단위·기능 단위로 만들고, 생성된 번호로 브랜치 이름과 PR 제목을 정한다.
- 제목: `[<prefix>] <설명>`. 예: `[feat] 약속 잡기 API 구현`, `[chore] spring data JPA 의존성 추가`.
- 제목에는 기능의 대표 설명을, 본문에는 세부 내용과 진행 상황을 적는다. 기능·설정·문서는 [feature 템플릿](../../.github/ISSUE_TEMPLATE/feature.md), 버그는 [bug 템플릿](../../.github/ISSUE_TEMPLATE/bug.md)을 쓰고, 버그 이슈 제목도 `[fix]`로 시작한다.
- 생성할 때 담당자(assignee)와 작업 내용 라벨을 지정한다. [자동 라벨](../../.github/workflows/auto-label.yml)이 제목의 prefix로 작업 유형 라벨을, 작성자로 팀원 라벨을 붙이므로 중복으로 붙이지 않는다.

## 브랜치

- 형식: `<prefix>/#<이슈번호>-<요약>`. 예: `feat/#1-login`, `fix/#13-login`.
- 요약은 작업을 구분하는 영문 키워드 1~4개를 소문자·하이픈으로 연결해 30자 이내로 쓴다. 공백과 이모지를 넣지 않는다.
- GitHub 이슈의 **Development → Create a branch** 또는 `gh issue develop <번호> --base develop --name '<브랜치명>' --checkout`으로 이슈에 연결해 만든다. 연결된 브랜치가 이미 있으면 재사용한다.

## 커밋

- 형식: `<prefix>(<Class>): <설명>`. 예: `feat(Member): 구글 로그인 API 기능 구현`, `chore(Member): lombok 의존성 추가`, `docs(README): 리드미 수정`.
- 초기 세팅처럼 여러 클래스를 한 번에 다루는 커밋은 클래스명을 생략한다. 예: `init: 프로젝트 초기 세팅`.
- 커밋은 촘촘하게 나눈다. 한 커밋은 하나의 검토 가능한 변경 이유만 담고, 의존성·설정, Entity·migration, Service, Controller·DTO, 문서가 분리 가능하면 각각 커밋한다. 결합된 변경을 억지로 나눠 중간 커밋의 빌드를 깨지는 않는다.
- `git add .`로 무관한 변경을 포함하지 않는다. 인증서·개인키나 실제 비밀번호·API 키·토큰이 커밋 후보에 있으면 커밋을 멈춘다. `--no-verify`로 검증을 건너뛰지 않는다.
- 커밋 전에 `git diff --check`와 `./gradlew build`로 확인하고, 실패를 성공으로 보고하지 않는다.

## PR

- `develop`과 `deploy`로 머지할 때는 PR이 필요하다. 본문에는 변경 사항 설명을 [PR 템플릿](../../.github/PULL_REQUEST_TEMPLATE.md)에 따라 적는다.
- 제목: `[<prefix>] #<이슈번호> - <설명>`. 예: `[feat] #1 - 약속 잡기 API 구현`, `[chore] #2 - spring data JPA 의존성 추가`.
- 머지 커밋 메시지: `[merge] #<이슈번호> <PR 제목>`.
- `closes #번호`의 자동 이슈 종료는 PR 대상이 GitHub 기본 브랜치(`develop`)일 때만 동작한다.
- `develop`·`deploy` 대상 PR은 [PR checks](../../.github/workflows/pr-check.yml)가 Java 21로 `./gradlew build`를 실행한다.
- PR을 올린 뒤 서버 톡방에 알린다. 이 알림은 담당자가 직접 한다.

## 코드 리뷰

- 코드 리뷰는 존댓말로 하며 일방적인 시비나 비난은 금지한다.
- CodeRabbit 리뷰도 함께 보면서 팀원이 직접 리뷰를 남긴다. CodeRabbit은 참고 자료이고 최종 판단은 팀원이 한다.

## 브랜치 보호

저장소 관리자가 GitHub Ruleset으로 `develop`과 `deploy`에 **PR 필수, `PR checks / build` 통과 필수, 승인 리뷰, 대화 해결**을 설정한다. 문서만으로는 머지를 강제할 수 없다.

실행 절차: [deary-issue](../../.agents/skills/git/deary-issue/SKILL.md) · [deary-commit](../../.agents/skills/git/deary-commit/SKILL.md) · [deary-push](../../.agents/skills/git/deary-push/SKILL.md) · [deary-pr](../../.agents/skills/git/deary-pr/SKILL.md).
