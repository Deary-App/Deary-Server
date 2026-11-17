# 인증과 JWT

인증 API, 토큰, 인증 사용자 식별, 보안 설정을 바꿀 때 읽는다.

## 흐름

- 앱은 Apple·Google 로그인으로 받은 ID 토큰을 서버에 보내고, 서버가 검증한 뒤 자체 토큰을 발급한다. Supabase Auth는 사용하지 않는다.
- 이후 인증이 필요한 요청은 `Authorization: Bearer <액세스 토큰>` 헤더로 보낸다.
- 발급·검증은 Spring Security OAuth2 Resource Server(Nimbus)로 한다. 설정은 `global/jwt/JwtConfig`, `SecurityConfig`에 있다.

## 소셜 로그인

| API | 요청 | 검증 |
| --- | --- | --- |
| `POST /api/v1/auth/apple` | `identityToken`, `nonce`(원문), 선택 `authorizationCode` | Apple JWKS 서명, `iss`=`https://appleid.apple.com`, `aud`∈`APPLE_CLIENT_IDS`, 만료, `nonce` 클레임 = 원문의 SHA-256(hex) |
| `POST /api/v1/auth/google` | `idToken` | Google JWKS 서명, `iss`∈`https://accounts.google.com`·`accounts.google.com`, `aud`∈`GOOGLE_CLIENT_IDS`, 만료 |

- 앱은 Apple 로그인 요청에 nonce의 SHA-256(hex)을 넣고, 서버에는 원문을 보낸다.
- 검증된 `sub`로 `social_accounts`를 찾고, 없으면 `users`·`social_accounts`를 만든다. 동시에 첫 로그인이 들어와도 `(provider, provider_user_id)` unique 제약으로 하나만 생성되고, 늦은 요청은 기존 계정으로 로그인한다.
- 이메일은 Apple은 토큰의 `email`, Google은 `email_verified`가 `true`일 때만 저장한다.
- 응답은 `SocialLoginResponse`(두 토큰, 각 만료 초, `newUser`)다.
- 토큰 검증 실패는 `AUTH_INVALID_SOCIAL_TOKEN`(401), 공개키(JWKS)를 가져오지 못하면 `AUTH_SOCIAL_PROVIDER_UNAVAILABLE`(503)이다. 공개키는 라이브러리가 캐시한다.
- `APPLE_CLIENT_IDS`(앱 Bundle ID), `GOOGLE_CLIENT_IDS`(Google OAuth 클라이언트 ID)는 쉼표로 여러 개를 넣을 수 있다. 비어 있으면 모든 소셜 토큰이 거절된다.

## 토큰

| 구분 | 형식 | 만료 | 저장 |
| --- | --- | --- | --- |
| 액세스 토큰 | HS256 서명 JWT. `iss`=`jwt.issuer`, `sub`=사용자 ID, `sid`=세션 ID(`refresh_tokens.id`), `iat`, `exp` | 30분 (`JWT_ACCESS_TOKEN_EXPIRATION`) | 저장하지 않음. `sid`로 세션을 확인 |
| 리프레시 토큰 | 32바이트 임의 값의 Base64URL 문자열 | 14일 (`JWT_REFRESH_TOKEN_EXPIRATION`) | `refresh_tokens`에 SHA-256 해시만 저장 |

- 토큰 발급은 `TokenService.generateTokens(userId)`로 하고, 응답은 `TokenIssueResponse`(두 토큰과 각 만료 초)다.
- 리프레시 토큰 원문은 응답으로 한 번만 내려주고 DB·로그에 남기지 않는다. 재발급할 때마다 새 토큰으로 교체(rotation)하고 이전 토큰은 폐기한다.
- 만료 검증에는 Spring Security 기본 허용 오차 60초가 적용된다.
- 서명 키 `JWT_SECRET`은 32바이트 이상이어야 하며 짧으면 앱이 시작되지 않는다. 모든 프로필에서 환경변수로만 받으며 기본값을 두지 않는다. 값이 없으면 앱이 시작되지 않는다. 테스트는 `build.gradle`의 `test` 작업이 테스트 전용 값을 넣는다.

## 재발급·로그아웃·탈퇴

| API | 인증 | 동작 |
| --- | --- | --- |
| `POST /api/v1/auth/reissue` | 없음 | `refreshToken`으로 새 토큰 쌍 발급. 같은 세션 행을 유지하고 토큰 해시와 만료만 교체한다. 없거나 만료됐거나 이미 쓴 토큰은 `AUTH_INVALID_REFRESH_TOKEN`(401) |
| `POST /api/v1/auth/logout` | 없음 | 이 기기의 `refreshToken`(세션)만 삭제. 이 세션의 액세스 토큰도 즉시 거절된다. 선택 값 `pushToken`을 보내면 리프레시 토큰 주인의 푸시 토큰일 때만 함께 삭제. 없는 토큰이어도 성공 |
| `DELETE /api/v1/users/me` | 필요 | 사용자를 삭제하고 FK `on delete cascade`로 소셜 계정·리프레시 토큰을 함께 삭제. 모든 기기의 액세스 토큰이 즉시 거절된다. 복구 불가 |

- 재발급은 `id`와 이전 토큰 해시를 조건으로 한 `update` 결과 행 수로 사용 여부를 판단한다. 같은 토큰으로 동시에 요청하면 한 요청만 새 토큰을 받는다.
- 재발급 때 세션 행을 지우고 새로 만들지 않는다. 행을 바꾸면 `sid`가 달라져 재발급 중에 나간 다른 요청이 401을 받기 때문이다. 재발급 전 액세스 토큰도 같은 `sid`라 만료까지 쓸 수 있다.
- 만료된 토큰으로 재발급을 요청하면 그 세션을 삭제한 뒤 401로 응답한다.
- 액세스 토큰 블랙리스트(jti)는 두지 않는다. 세션 확인으로 기기별 로그아웃·탈퇴를 처리하고, 계정 정지나 전체 기기 로그아웃도 해당 사용자의 `refresh_tokens`를 지우면 된다.
- 회원 데이터를 담는 새 테이블은 `users`를 `on delete cascade`로 참조해 탈퇴 시 함께 지워지게 한다. 스토리지 파일(사진)은 해당 기능에서 삭제 처리를 추가한다.

## Apple 계정 연결 해지

App Store 가이드라인 5.1.1(v)에 따라 탈퇴할 때 Sign in with Apple 연결을 해지한다.

- 로그인 요청에 `authorizationCode`가 있으면 서버가 `https://appleid.apple.com/auth/token`으로 교환해 refresh token을 받는다. `client_id`는 identity token의 `aud`(Bundle ID), `client_secret`은 `.p8` 키로 서명한 ES256 JWT(5분 유효)다.
- refresh token은 `social_accounts.apple_refresh_token`에 AES-256-GCM으로 암호화해 저장하고, 교환에 쓴 `client_id`를 `apple_client_id`에 함께 저장한다. 암호화 키는 `JWT_SECRET`에서 파생하므로 `JWT_SECRET`을 바꾸면 기존 값은 복호화되지 않아 해지가 건너뛰어진다. 토큰 원문은 응답·로그에 남기지 않는다.
- 탈퇴 시 사용자 삭제 전에 저장된 토큰을 읽고, 트랜잭션 커밋 후 `https://appleid.apple.com/auth/revoke`로 해지한다.
- 교환·해지 실패, 설정 누락, 저장된 토큰 없음은 경고 로그만 남기고 로그인·탈퇴는 성공시킨다.
- 설정: `APPLE_TEAM_ID`, `APPLE_KEY_ID`, `APPLE_PRIVATE_KEY`(`.p8` 내용, 줄바꿈은 `\n`으로 넣어도 된다). 하나라도 비면 교환·해지를 건너뛴다.

## 인증 사용자 식별

- 검증된 토큰은 `AuthUserAuthenticationConverter`가 `AuthUser(userId)` principal로 바꾼다. 이때 `AuthSessionVerifier`로 `sid` 세션이 그 사용자의 것이고 만료되지 않았는지 요청마다 DB에서 확인한다(1회 조회). 구현은 `domain/auth`의 `AuthSessionService`다.
- `sid`가 없거나 숫자가 아닌 토큰, 세션이 없거나 만료되었거나 다른 사용자의 세션인 토큰은 401이다. `sid` 도입 전에 발급된 토큰도 401이며, 앱은 리프레시 토큰으로 재발급한다.
- Controller는 `@AuthenticationPrincipal AuthUser authUser`로 받아 `authUser.userId()`를 Service에 넘긴다. 클라이언트가 보낸 사용자 ID로 소유권을 판단하지 않는다.
- 토큰이 없거나 위조·만료·형식 오류이면 `CommonErrorCode.UNAUTHORIZED`(401) `ErrorDto`로 응답한다.

## 공개 경로

- 인증 없이 열려 있는 경로는 `SecurityConfig`의 `permitAll` 목록이다. 로그인처럼 공개가 필요한 API를 추가하면 이 목록에 경로를 정확히 추가한다.
- Swagger는 `bearerAuth` 보안 스킴이 설정돼 있어 Authorize 버튼에 액세스 토큰을 넣고 시험할 수 있다. `prod` 프로필에서는 Swagger가 꺼져 있다.
