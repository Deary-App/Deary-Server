# 아키텍처

새 도메인·레이어·의존성을 설계할 때 읽는다.

## 구조

- 하나의 Gradle 모듈이며 Java 소스는 `src/main/java/com/deary/server/`에 있다. 모듈 분리는 별도 구조 변경 작업으로 다룬다.
- `domain/{기능}/`: 일기·교정·편지 등 기능별로 아래 하위 패키지를 둔다.

```text
domain/{기능}/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
│   ├── request/
│   └── response/
└── exception/
```

- `global/`: 인증·JWT, 설정(`config/`), 공통 응답, 공통 예외처럼 여러 도메인이 공유하는 코드를 둔다. 한 도메인에서만 쓰는 코드는 해당 도메인에 둔다.
- `DearyServerApplication.java`가 진입점이다. 의존성은 루트 `build.gradle`에서 관리한다.

## 의존 방향과 책임

`Controller → Service → Repository/Entity`가 기본 흐름이다. Controller는 HTTP 입력·응답과 인증 사용자 식별을, Service는 비즈니스 규칙·트랜잭션·도메인 간 조율을, Repository는 조회와 저장을 맡는다. Entity에는 자신의 상태와 불변식을 다루는 메서드를 둘 수 있다.

- Repository가 Service나 Controller를 참조하지 않는다.
- 다른 도메인이 필요하면 그 도메인의 공개 Service나 조회 기능을 사용하고, 다른 도메인의 DTO·Entity 내부를 직접 조작하지 않는다.
- 인터페이스의 구현체에는 `Impl` 접미사를 붙인다.
- 새 패키지나 추상화를 만들기 전에 같은 도메인의 기존 구현을 살핀다.
