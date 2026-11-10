# JPA와 스키마 변경

Entity·Repository·트랜잭션 또는 테이블·컬럼을 바꿀 때 읽는다.

## Entity 작성

- `@Table`, `@Column` 등 매핑 어노테이션을 모두 명시한다. 코드에서 필드 이름이 바뀌어도 테이블 매핑이 유지되게 하기 위해서다.
- 필드는 아래 순서로 작성한다.
  1. 기본 키 등 식별 필드
  2. 일반 정보 필드
  3. 다른 테이블과의 연관관계
- 테이블·컬럼 이름은 `snake_case`, 날짜·시각 필드는 `...At`·`...Date`로 짓는다.
- 목록 조회에서 연관관계 로딩과 N+1 가능성을 확인한다.
- 기본 키는 `bigint` identity(`@GeneratedValue(strategy = GenerationType.IDENTITY)`)를 쓴다.
- enum 컬럼은 `@Enumerated(EnumType.STRING)`과 `varchar`로 저장하고, migration에 허용 값 `check` 제약을 둔다.

## 생성·수정 시각

- 테이블에는 `created_at`, `updated_at`(`timestamptz not null`)을 둔다.
- Entity는 `global/entity/BaseTimeEntity`를 상속한다. JPA Auditing(`global/config/JpaAuditingConfig`)이 저장·수정 시 `createdAt`·`updatedAt`(`Instant`, UTC)을 채운다. 직접 값을 넣지 않는다.

## 조회와 트랜잭션

- 사용자별 데이터 조회에는 소유권 조건을 빠뜨리지 않는다.
- 상태 변경은 Service의 트랜잭션 안에서 수행하고, 읽기 전용 조회는 `@Transactional(readOnly = true)`를 쓴다.
- 중복 요청이나 동시 요청이 생길 수 있는 쓰기는 unique 제약, 잠금, 멱등 처리 중 무엇으로 막을지 함께 정한다.

## 스키마 변경 (Flyway)

- DB는 PostgreSQL 17이다. `spring.jpa.hibernate.ddl-auto`는 `validate`이므로 스키마는 `src/main/resources/db/migration/`의 Flyway 파일로만 바꾼다.
- 파일 이름은 `V<번호>__<설명>.sql`이다. 이미 공유되거나 적용된 migration 파일은 수정·삭제하지 않고 새 버전 파일을 추가한다.
- Supabase 대시보드에서 테이블을 직접 만들거나 고치지 않는다.
- 사용자 삭제처럼 함께 지워져야 하는 하위 데이터는 FK에 `on delete cascade`를 두고, 그렇지 않은 관계는 Service에서 삭제 순서를 명시한다.
- unique·check·FK 제약과 인덱스 이름은 `uk_`·`ck_`·`idx_` + 테이블명 + 컬럼 의미로 짓는다.
- 컬럼 삭제·타입 변경은 기존 데이터, NULL·기본값, 이전 버전 애플리케이션과의 호환성, 인덱스와 잠금 영향을 함께 검토한다.
- Entity 매핑과 migration을 같은 변경에 포함하고, `./gradlew build`의 Testcontainers 테스트로 매핑이 맞는지 확인한다.
