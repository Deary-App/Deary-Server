# Java·API 코딩 스타일

Controller, Service, Repository, DTO 또는 API를 작성·리뷰할 때 읽는다.

## 네이밍

| 대상 | 규칙 | 예 |
| --- | --- | --- |
| 클래스 | `PascalCase`. and·or 같은 접속사를 쓰지 않고 25자 내외 | `DiaryController` |
| 메서드·변수 | `camelCase` | `findDiary`, `diaryId` |
| DB 테이블·컬럼 | `snake_case` | `diary_revision`, `created_at` |
| enum 값·상수 | `UPPER_SNAKE_CASE` | `MAX_PHOTO_COUNT` |
| 컬렉션 | 복수형. `List` 같은 타입명을 붙이지 않는다 | `users` (O), `userList` (X) |
| 날짜·시각 | 접미사 `Date` 또는 `At` | `createdAt`, `diaryDate` |

## 클래스명

패키지 역할을 접미사로 드러낸다. 구현체는 `Impl`을 붙인다(예: `UserServiceImpl`, `UserRepositoryImpl`).

| 패키지 | 예 |
| --- | --- |
| controller | `UserController` |
| service | `UserService` |
| repository | `UserRepository` |
| dto/request | `UserSaveRequest` |
| dto/response | `UserFindResponse` |
| entity | `User` |
| config | `WebConfig` |
| exception | `UserNotFoundException`, `UserErrorCode` |

## 메서드 작성

- public 메서드를 모두 위에 두고, 그 아래에 private 메서드를 둔다.
- 메서드 하나는 하나의 역할만 한다. 이름은 목적을 동사로 시작하고 의도가 드러나게 간결하게 쓴다.
- event, design처럼 뜻이 두 가지인 단어는 피한다.
- 부수효과를 이름에 드러낸다. 조회하다가 없으면 생성한다면 `getTemp()`가 아니라 `getOrCreateTemp()`다.

## 메서드 이름

일반 메서드(Repository 등)는 `(조작 행위) - [여러 건] - (자원) - [조건]` 순서로 짓는다.

| 의미 | 예 |
| --- | --- |
| 저장 | `saveUser()` |
| 수정 | `updateUser()` |
| 삭제 | `deleteUser()` |
| 조회 | `findUser()` |
| 여러 건 조회 | `findAllUser()` |
| 조건 조회 | `findUserByName()` |
| 여러 건 조건 조회 | `findAllUserByName()` |

Controller와 Service는 아래 동사로 시작한다.

| 유형 | Controller | Service |
| --- | --- | --- |
| 조회 | `read...()` | `find...()` |
| 등록 | `create...()` | `generate...()` (생성과 등록을 나누면 생성 `generate`, 등록 `register`) |
| 변경 | `update...()` | `modify...()` |
| 삭제 | `delete...()` | `remove...()` |
| 등록·수정·삭제가 함께 일어남 | `process...()` | `handle...()` |

## DTO 이름

- 요청·응답 DTO는 Java `record`로 만들고 `...Request`, `...Response` 접미사를 붙인다.
- 요청 DTO: `(자원) - (조작 행위)`. 응답 DTO: `(자원) - (조작 행위) - [여러 건] - [조건]`.

| 종류 | 예 |
| --- | --- |
| 요청 - 저장 | `UserSaveRequest` |
| 요청 - 수정 | `UserModifyRequest` |
| 요청 - 삭제 | `UserRemoveRequest` |
| 요청 - 조회 | `UserFindRequest` |
| 응답 - 조회 | `UserFindResponse` |
| 응답 - 여러 건 조회 | `UserFindAllResponse` |
| 응답 - 조건 조회 | `UserFindByNameResponse` |
| 응답 - 여러 건 조건 조회 | `UserFindAllByNameResponse` |

## URL

RESTful API 설계 가이드를 따른다.

- `get`, `put`처럼 HTTP 메서드로 구분되는 행위를 URL에 넣지 않는다.
- 마지막에 `/`를 붙이지 않는다.
- `_` 대신 `-`를 쓰고 소문자만 쓴다.
- 확장자를 넣지 않는다.
- 자원은 복수형으로 쓴다.

```text
/api/v1/auth
/api/v1/users
/api/v1/diaries
/api/v1/letters
```

## 레이어별 작성

- Controller는 HTTP 매핑, `@Valid` 입력 검증, 인증 사용자 식별, 응답 상태·형식에 집중한다. 비즈니스 로직을 넣지 않는다.
- Service는 권한·소유권·비즈니스 규칙을 확인하고 Repository를 호출한다. 상태 변경은 트랜잭션 안에서, 읽기 전용 조회는 `@Transactional(readOnly = true)`로 한다.
- Repository는 `JpaRepository`와 필요한 조회 메서드를 사용한다. 결과가 없을 수 있으면 `Optional`로 드러낸다.
- 요청 필드에는 Bean Validation을 붙이고, 사용자가 읽는 검증 메시지는 한국어로 구체적으로 쓴다.
- API를 추가·변경하면 springdoc 문서의 요청·응답·오류 설명도 실제 동작과 맞춘다.
