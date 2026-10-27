# Deary Server 컨벤션 인덱스

필요한 주제만 읽는다. 여러 영역을 바꾸면 해당 문서를 함께 확인하고, 기존 코드와 규칙이 어긋나면 관련 구현을 먼저 살핀다.

| 문서 | 담당 영역 | 읽는 때 |
| --- | --- | --- |
| [architecture.md](architecture.md) | 단일 모듈, 도메인·공통 패키지, 레이어 책임 | 새 도메인·서비스 경계 설계 |
| [coding-style.md](coding-style.md) | 네이밍, 메서드·DTO 이름, URL, 레이어별 작성 | Java API 코드 작성·리뷰 |
| [api-response.md](api-response.md) | `BaseResponse`, `ErrorDto`, 예외 처리 | 응답·예외·오류 코드 변경 |
| [persistence.md](persistence.md) | Entity 작성 규칙, JPA, Flyway·PostgreSQL | DB 조회·테이블·컬럼 변경 |
| [git-convention.md](git-convention.md) | 이슈·브랜치·커밋·PR·리뷰·배포 | Git 작업 |
| [automation.md](../automation.md) | 요청별 실행 단계·중단 조건·CI와 사람의 판단 지점 | 반복 작업 자동화 설계·점검 |

예: 새로운 일기 API는 `architecture.md`와 `coding-style.md`를 읽고, 새 테이블이 있으면 `persistence.md`, 새 오류 코드가 있으면 `api-response.md`를 추가로 읽는다.
