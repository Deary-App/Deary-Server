# Deary Server

Deary 앱의 API 서버다. Java 21, Spring Boot 4.1, PostgreSQL 17 기반이며 DB는 Supabase PostgreSQL을 저장소로만 사용한다.

이전 Supabase RPC·Edge Function 구현은 `legacy/supabase` 브랜치에 보관한다. 새 스키마 설계 시 테이블과 권한 규칙의 참고 자료로 사용한다.

| 경로 | 내용 |
|---|---|
| src/main/java/com/deary/server/ | 애플리케이션 코드 |
| src/main/resources/db/migration/ | Flyway migration |
| src/main/resources/application*.yml | 공통·local·prod 설정 |
| compose.yaml | 로컬 PostgreSQL 17 |
| docs/ | 제품 정책·로드맵·검증 기준 |

## 로컬 실행

```sh
docker compose up -d
./gradlew bootRun
```

기본 프로필은 `local`이며 `localhost:5432/deary`에 접속한다. 다른 DB를 쓰려면 `.env.example`의 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`를 환경변수로 지정한다. Swagger는 `/swagger-ui/index.html`, 상태 확인은 `/actuator/health`다.

## 검사

```sh
./gradlew build
```

테스트는 Testcontainers로 PostgreSQL 17 컨테이너를 띄우므로 Docker가 실행 중이어야 한다.

## 운영 설정

`prod` 프로필은 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`가 필요하고 Swagger를 끈다. Supabase에 연결할 때는 Supavisor Session mode 주소를 사용한다. 스키마는 Flyway로만 변경하고 Supabase 대시보드에서 직접 수정하지 않는다. Supabase의 Data API는 끈다.
