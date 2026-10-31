# Deary Server 에이전트 작업 기준

이 파일은 Codex와 Claude가 Deary 서버에서 **작업을 실제로 진행할 때** 지킬 공통 기준이다. Java 21·Spring Boot 4.1·Gradle 단일 모듈이며 패키지 루트는 `com.deary.server`다. DB는 PostgreSQL(Supabase)이고 스키마는 Flyway로만 바꾼다. `CLAUDE.md`는 `@AGENTS.md`로 이 파일을 가져온다.

## 프로젝트 구조

- `src/main/java/com/deary/server/domain/{기능}/`에는 기능별 controller, service, repository, entity, dto, exception을 둔다. 새 기능은 가장 가까운 기존 도메인의 구조를 먼저 확인한다.
- `global/`에는 인증·JWT, 설정, 공통 응답·예외처럼 여러 도메인이 공유하는 코드를 둔다. 한 도메인만 쓰는 코드를 `global/`에 두지 않는다.
- 의존 흐름은 `Controller → Service → Repository/Entity`다. Repository가 Service나 Controller를 참조하지 않는다.
- `src/main/resources/db/migration/`이 스키마의 원본이다. `ddl-auto`는 `validate`이므로 Entity만 바꾸고 migration을 빠뜨리지 않는다.
- `docs/`의 ACCEPTANCE·ANALYTICS·POLICIES·ROADMAP·SECURITY·VERIFICATION은 제품 정책·로드맵 문서다. 기능 규칙이 필요할 때 해당 문서만 읽는다.

## 작업 원칙

**구현 전에 확실하지 않은 것은 반드시 사용자에게 질문한다.**

- 요구사항이 여러 방식으로 해석될 수 있으면 임의로 선택하지 말고 선택지를 제시하고 물어본다.
- 컨벤션 문서에 없는 새로운 패턴을 도입하거나 패키지·레이어 위치가 애매하면 먼저 물어본다.
- 요청받은 범위만 수정한다. 인접 코드 개선·리팩토링은 임의로 하지 않고, 기존 사용자 변경을 되돌리지 않는다.
- 테스트 코드는 명시적으로 요청받지 않으면 작성하지 않는다. 기존 테스트를 실행해 변경을 검증하는 것은 계속 수행한다.
- 변경 전에 `git status --short --branch`로 브랜치와 기존 변경을 확인하고, 관련 코드·호출부·빌드 설정을 읽는다. 새 대화에서도 Git 상태와 연결 이슈·PR을 확인해 중단된 단계부터 이어간다.
- 비밀번호·토큰·API 키·인증서, 인증 헤더와 개인정보를 코드·문서·커밋·로그·PR 본문에 노출하지 않는다. 로컬 실행에는 실제 비밀값을 커밋하지 않는 `.env` 계열 파일만 사용한다.
- 커밋·PR·이슈·코멘트에 AI 도구 서명(Co-Authored-By, Generated with 등)을 넣지 않는다.
- 사용자에게 하는 설명과 완료 보고는 한국어로 쓴다.

## Java와 API의 핵심 경계

- Controller에는 HTTP 매핑, `@Valid` 요청 검증, 인증 사용자 식별과 응답 변환을 둔다. Service에는 비즈니스 결정, 권한·소유권 확인, 트랜잭션을 둔다. Entity를 API 응답에 직접 노출하지 않는다.
- 클라이언트가 보낸 사용자 ID만으로 소유권을 판단하지 않고 인증 principal의 ID를 사용한다.
- 토큰 지급·차감, 편지 발송, 교정 선점, 좋아요처럼 정합성이 중요한 로직은 트랜잭션·잠금·unique 제약·멱등 처리를 함께 검토한다.
- 이름·DTO·URL은 [coding-style.md](docs/conventions/coding-style.md), 응답·오류 형식은 [api-response.md](docs/conventions/api-response.md)를 따른다.
- 설정은 `application.yml`, `application-local.yml`, `application-prod.yml`을 함께 확인한다. 새 환경변수는 `.env.example`과 두 프로필을 함께 갱신하고 실제 값은 저장소에 넣지 않는다.

## 단계별 실행 경계

전체 흐름과 사람의 확인 지점은 [automation.md](docs/automation.md), 명명·형식 규칙은 [git-convention.md](docs/conventions/git-convention.md)를 따른다. 실제 트리거·확인 순서·중단 조건은 각 스킬을 원본으로 삼는다.

- 이슈·브랜치는 명시적인 준비 요청에서만 [deary-issue](.agents/skills/git/deary-issue/SKILL.md)로 처리한다.
- 구현은 관련 컨벤션과 기존 코드 패턴을 확인한 뒤 진행한다. 새 작업에 이슈·작업 브랜치가 없으면 준비 단계를 먼저 안내한다. Java 변경은 `./gradlew build`, 제출 전에는 `git diff --check`로 검증한다.
- 커밋 계획과 로컬 커밋은 [deary-commit](.agents/skills/git/deary-commit/SKILL.md)을 따른다. 계획 직후의 "푸시해줘"는 계획 승인도 포함하며, 변경이 그대로일 때만 커밋 후 푸시한다.
- 푸시는 [deary-push](.agents/skills/git/deary-push/SKILL.md), PR은 [deary-pr](.agents/skills/git/deary-pr/SKILL.md)에 따라 명시적인 요청에서만 실행한다.
- 리뷰·머지·`deploy` 배포와 서버 톡방 전송은 사람의 판단 또는 명시적인 요청 없이 진행하지 않는다.

## 작업별 상세 문서

**현재 작업에 해당하는 행만 읽는다.** 전체 목록은 [컨벤션 인덱스](docs/conventions/00-index.md)에 있다.

| 작업 | 읽을 문서 |
| --- | --- |
| 새 도메인, 패키지·레이어 설계 | [architecture.md](docs/conventions/architecture.md) |
| 클래스·메서드·DTO 이름, URL, Controller·Service 작성 | [coding-style.md](docs/conventions/coding-style.md) |
| 성공 응답, 예외, 오류 응답 | [api-response.md](docs/conventions/api-response.md) |
| Entity·Repository, 테이블·컬럼 변경, Flyway | [persistence.md](docs/conventions/persistence.md) |
| 이슈·브랜치·커밋·PR·리뷰·배포 | [git-convention.md](docs/conventions/git-convention.md) |
| 자동화 절차 설계·수정 | [automation.md](docs/automation.md) |

완료 보고에는 바뀐 파일, 실제 실행한 검증과 결과, 실행하지 못한 검증의 이유, 남은 사람의 판단 지점을 적는다. 커밋·푸시·PR·머지를 실행하지 않았다면 완료한 것처럼 말하지 않는다.
