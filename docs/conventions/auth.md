# 인증과 JWT

인증 API, 토큰, 인증 사용자 식별, 보안 설정을 바꿀 때 읽는다.

## 흐름

- 앱은 Apple·Google 로그인으로 받은 ID 토큰을 서버에 보내고, 서버가 검증한 뒤 자체 토큰을 발급한다. Supabase Auth는 사용하지 않는다.
- 이후 인증이 필요한 요청은 `Authorization: Bearer <액세스 토큰>` 헤더로 보낸다.
- 발급·검증은 Spring Security OAuth2 Resource Server(Nimbus)로 한다. 설정은 `global/jwt/JwtConfig`, `SecurityConfig`에 있다.

## 토큰

| 구분 | 형식 | 만료 | 저장 |
| --- | --- | --- | --- |
| 액세스 토큰 | HS256 서명 JWT. `iss`=`jwt.issuer`, `sub`=사용자 ID, `iat`, `exp` | 30분 (`JWT_ACCESS_TOKEN_EXPIRATION`) | 저장하지 않음 |
| 리프레시 토큰 | 32바이트 임의 값의 Base64URL 문자열 | 14일 (`JWT_REFRESH_TOKEN_EXPIRATION`) | `refresh_tokens`에 SHA-256 해시만 저장 |

- 토큰 발급은 `TokenService.generateTokens(userId)`로 하고, 응답은 `TokenIssueResponse`(두 토큰과 각 만료 초)다.
- 리프레시 토큰 원문은 응답으로 한 번만 내려주고 DB·로그에 남기지 않는다. 재발급할 때마다 새 토큰으로 교체(rotation)하고 이전 토큰은 폐기한다.
- 만료 검증에는 Spring Security 기본 허용 오차 60초가 적용된다.
- 서명 키 `JWT_SECRET`은 32바이트 이상이어야 하며 짧으면 앱이 시작되지 않는다. 모든 프로필에서 환경변수로만 받으며 기본값을 두지 않는다. 값이 없으면 앱이 시작되지 않는다. 테스트는 `build.gradle`의 `test` 작업이 테스트 전용 값을 넣는다.

## 인증 사용자 식별

- 검증된 토큰은 `AuthUserAuthenticationConverter`가 `AuthUser(userId)` principal로 바꾼다.
- Controller는 `@AuthenticationPrincipal AuthUser authUser`로 받아 `authUser.userId()`를 Service에 넘긴다. 클라이언트가 보낸 사용자 ID로 소유권을 판단하지 않는다.
- 토큰이 없거나 위조·만료·형식 오류이면 `CommonErrorCode.UNAUTHORIZED`(401) `ErrorDto`로 응답한다.

## 공개 경로

- 인증 없이 열려 있는 경로는 `SecurityConfig`의 `permitAll` 목록이다. 로그인처럼 공개가 필요한 API를 추가하면 이 목록에 경로를 정확히 추가한다.
- Swagger는 `bearerAuth` 보안 스킴이 설정돼 있어 Authorize 버튼에 액세스 토큰을 넣고 시험할 수 있다. `prod` 프로필에서는 Swagger가 꺼져 있다.
