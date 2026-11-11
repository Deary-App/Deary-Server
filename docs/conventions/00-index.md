# Deary Server 컨벤션 인덱스

필요한 주제만 읽는다. 여러 영역을 바꾸면 해당 문서를 함께 확인하고, 기존 코드와 규칙이 어긋나면 관련 구현을 먼저 살핀다.

| 문서 | 담당 영역 | 읽는 때 |
| --- | --- | --- |
| [architecture.md](architecture.md) | 단일 모듈, 도메인·공통 패키지, 레이어 책임 | 새 도메인·서비스 경계 설계 |
| [coding-style.md](coding-style.md) | 네이밍, 메서드·DTO 이름, URL, 레이어별 작성 | Java API 코드 작성·리뷰 |
| [api-response.md](api-response.md) | `BaseResponse`, `ErrorDto`, 예외 처리 | 응답·예외·오류 코드 변경 |
| [auth.md](auth.md) | 로그인 흐름, JWT·리프레시 토큰, 인증 사용자 식별, 공개 경로 | 인증·보안 설정 변경 |
| [correction.md](correction.md) | 교정 요청 상태, 선점·제출 규칙, 탈퇴 시 처리 | 교정 기능 변경 |
| [storage.md](storage.md) | 사진 업로드 URL, 경로 소유권, 버킷, 보기 URL | 사진 업로드·조회·삭제 |
| [logging.md](logging.md) | traceId 요청 추적, 로그 레벨, 기록 금지 정보 | 로그 추가·로그 형식 변경 |
| [persistence.md](persistence.md) | Entity 작성 규칙, JPA, Flyway·PostgreSQL | DB 조회·테이블·컬럼 변경 |
| [git-convention.md](git-convention.md) | 이슈·브랜치·커밋·PR·리뷰·배포 | Git 작업 |
| [automation.md](../automation.md) | 요청별 실행 단계·중단 조건·CI와 사람의 판단 지점 | 반복 작업 자동화 설계·점검 |

예: 새로운 일기 API는 `architecture.md`와 `coding-style.md`를 읽고, 새 테이블이 있으면 `persistence.md`, 새 오류 코드가 있으면 `api-response.md`를 추가로 읽는다.
