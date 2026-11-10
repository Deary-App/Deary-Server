# 클라이언트 연결 가이드

Deary 앱(Expo)이 Spring 서버 API에 연결할 때 보는 문서다. 화면 이름은 화면설계서 v2.0 MVP 기준이다. 요청·응답 필드의 정확한 타입과 예시는 서버를 띄운 뒤 Swagger(`/swagger-ui/index.html`)에서 확인한다.

## 1. 연결 준비

| 환경 | 서버 주소 |
| --- | --- |
| iOS 시뮬레이터 + 내 Mac에서 서버 실행 | `http://localhost:8080` |
| 실제 iPhone + 같은 와이파이의 Mac | `http://<Mac IP>:8080` (개발 빌드에 iOS `http` 예외 설정 필요) |

- 서버 실행: 서버 저장소에서 `.env.local`을 채운 뒤 `docker compose up -d` → `./gradlew bootRun`
- 모든 API 경로는 `/api/v1`로 시작한다.
- Supabase는 서버가 DB·Storage로만 쓴다. **앱은 Supabase에 직접 요청하지 않는다**(사진 파일 업로드·다운로드 URL만 예외, 아래 5장).

## 2. 공통 규칙

### 응답 형식

성공:

```json
{ "status": 200, "message": "기록을 조회했습니다.", "data": { } }
```

실패:

```json
{ "timestamp": "2026-11-11T01:00:00Z", "status": 409, "errorCode": "DIARY_ALREADY_WRITTEN_TODAY", "message": "오늘은 이미 기록을 남겼어요.", "path": "/api/v1/diaries" }
```

- 화면 분기는 HTTP 상태와 `errorCode`로 하고, 사용자에게는 `message`를 그대로 보여줘도 된다(한국어 문장).
- 입력 검증 실패는 400 `COMMON_INVALID_INPUT`이며 `message`에 첫 번째 필드의 안내 문구가 들어 있다.

### 인증

- 로그인(S01-2)으로 받은 `accessToken`을 모든 인증 API에 `Authorization: Bearer <accessToken>`으로 보낸다.
- 액세스 토큰은 30분, 리프레시 토큰은 14일 유효하다. 두 토큰은 기기 보안 저장소(SecureStore)에 저장한다.
- 401 `COMMON_UNAUTHORIZED`를 받으면 `POST /auth/reissue`로 재발급한 뒤 **원래 요청을 한 번만** 다시 보낸다. 재발급도 401(`AUTH_INVALID_REFRESH_TOKEN`)이면 토큰을 지우고 로그인 화면으로 보낸다.
- 재발급할 때마다 리프레시 토큰도 새로 바뀐다. 이전 리프레시 토큰은 다시 쓸 수 없으니 반드시 새 값으로 교체한다. 동시에 여러 요청이 401을 받아도 재발급은 한 번만 하도록 묶는다.

### 값 형식

| 항목 | 값 |
| --- | --- |
| 언어 | `ko`, `ja`, `en` |
| 학습 수준 | `HIGH`(상), `MEDIUM`(중), `LOW`(하) |
| 성별 | `FEMALE`(여), `MALE`(남), `UNDISCLOSED`(밝히지 않음) |
| 국적 | ISO 3166-1 국가 코드 2자리 대문자 (`KR`, `JP`, `US`) |
| 날짜 | `YYYY-MM-DD` (일기 날짜는 서버가 한국시간 기준으로 정한다) |
| 시각 | UTC ISO-8601 (`2026-11-11T01:00:00Z`) |
| 교정 진행 상태 | `WAITING`(교정 대기 중), `IN_PROGRESS`(교정 중), `COMPLETED`(교정 완료) |

## 3. 화면별 API

### S01-1 스플래시

1. 저장된 토큰이 없으면 → S01-2 로그인
2. `GET /users/me/registration` → `{ termsAgreed, profileCompleted }`
   - `termsAgreed=false` → S02-0 약관 동의
   - `profileCompleted=false` → S02-1 회원가입 정보
   - 둘 다 `true` → S03 홈
   - 401(재발급 실패 포함)·404 `USER_NOT_FOUND` → 토큰 삭제 후 S01-2

### S01-2 로그인

| 버튼 | API | 보낼 값 |
| --- | --- | --- |
| Apple | `POST /auth/apple` | `identityToken`, `nonce`(원문) |
| Google | `POST /auth/google` | `idToken` |

- 응답: `accessToken`, `refreshToken`, `accessTokenExpiresIn`, `refreshTokenExpiresIn`(초), `newUser`
- 성공하면 S01-1 분기(가입 상태 조회)로 보낸다.
- **Apple:** `AppleAuthentication.signInAsync`에 원문 nonce의 **SHA-256(hex)**을 `nonce`로 넣고, 서버에는 **원문**을 보낸다(지금 코드의 `rawNonce`·`hashedNonce` 그대로).
- **Google:** Supabase 웹 OAuth(`signInWithOAuth`) 대신 **네이티브 Google 로그인으로 ID 토큰**을 받아야 한다(예: `@react-native-google-signin/google-signin`). 앱에 설정하는 클라이언트 ID는 서버 `GOOGLE_CLIENT_IDS`와 같아야 한다.
- 실패: 401 `AUTH_INVALID_SOCIAL_TOKEN`(토큰 검증 실패), 503 `AUTH_SOCIAL_PROVIDER_UNAVAILABLE`(잠시 후 재시도). 사용자가 로그인 시트를 취소하면 서버를 부르지 않는다.

### S02-0 약관 동의 / S02-0a 약관 본문

1. 화면 열 때 `GET /terms` → `terms[]: { termsId, type, version, title, required }`
2. [보기] → `GET /terms/{termsId}` → `content`(본문)
3. [동의하고 계속] → `POST /users/me/consents`

```json
{ "agreements": [
  { "termsId": 1, "agreed": true }, { "termsId": 2, "agreed": true },
  { "termsId": 3, "agreed": true }, { "termsId": 4, "agreed": false }
] }
```

- 화면에 보이는 **모든 약관**을 보낸다(선택 약관도 `agreed`로). `type`: `SERVICE`, `PRIVACY`, `AGE_OVER_14`(필수), `MARKETING`(선택)
- 실패: 400 `TERMS_REQUIRED_NOT_AGREED`, `TERMS_NOT_CURRENT`(약관이 바뀜 → 목록 다시 조회)
- 약관 조회 API는 로그인 없이도 호출할 수 있다.
- 뒤로가기 "가입을 그만둘까요?" → 로그아웃(S13-1과 같은 API) 후 S01-2

### S02-1 회원가입 정보 / S13-3 프로필 수정

1. (사진을 골랐다면) 프로필 사진 업로드 → 5장의 `POST /uploads/profile-image` 흐름으로 `path`를 얻는다.
2. [저장] → `PUT /users/me/profile`

```json
{ "nickname": "예나", "birthDate": "2000-01-01", "nationality": "KR",
  "nativeLanguage": "ko", "learningLanguage": "ja", "learningLevel": "MEDIUM",
  "gender": "FEMALE", "profileImagePath": "profiles/1/…", "version": 0 }
```

- 처음 저장할 때는 `version`을 빼고, **수정할 때는 `GET /users/me/profile`로 받은 `version`을 그대로** 보낸다.
- 응답은 저장된 프로필(`profileImageUrl`, 새 `version` 포함)이다.
- 실패
  - 403 `PROFILE_TERMS_NOT_AGREED`
  - 400 `PROFILE_UNDER_MINIMUM_AGE`, `PROFILE_SAME_LANGUAGE`, `PROFILE_INVALID_NATIONALITY`, `PROFILE_INVALID_IMAGE`, `PROFILE_VERSION_REQUIRED`
  - 409 `PROFILE_VERSION_CONFLICT`(다른 기기에서 먼저 수정 → 다시 조회 후 저장)
- 수정 화면을 열 때: `GET /users/me/profile`

### S03 홈

- `GET /home` → `{ nickname, profileImageUrl, hasUnreadCorrection }`
- `hasUnreadCorrection=true`이면 [이전 기록] 탭에 빨간 점을 띄운다.

### S04-1 새 기록 → S04-4 전송

1. 화면 열 때 `GET /diaries/today` → `{ today, written, diaryId }`. `written=true`면 [오늘 기록 열기]로 S05(`diaryId`)
2. 사진 업로드: `POST /uploads/diary-images` `{ "count": 2 }` → `uploads[]: { path, uploadUrl }` → 각 `uploadUrl`에 파일 업로드(5장)
3. [완료] → `POST /diaries` (201)

```json
{ "title": "오늘", "content": "今日は晴れでした。",
  "photos": [ { "path": "diaries/1/…", "positionX": 0.2, "positionY": 0.3, "scale": 1.2, "rotation": -15 } ] }
```

- `photos`는 1–3장. `positionX`·`positionY`는 종이 기준 0–1 상대 위치, `scale`은 0–10 배율, `rotation`은 −360~360도
- 응답: `{ diaryId, firstPhotoUrl }` → S04-4에 첫 사진을 보여주고 3.5초 뒤 S05(`diaryId`)로 이동
- 저장과 동시에 공개되고 사람 교정 요청이 만들어진다(앱에서 따로 요청하지 않는다).
- 실패
  - 409 `DIARY_ALREADY_WRITTEN_TODAY`
  - 400 `DIARY_INVALID_PHOTO`(업로드 URL 발급으로 받은 경로가 아님), `DIARY_EMPTY_CONTENT`, `COMMON_INVALID_INPUT`(제목 80자·본문 2000자 초과 등)
  - 404 `PROFILE_NOT_FOUND`
- 작성 중 임시저장(S04-3)은 기기에만 저장하고 서버와 주고받지 않는다.

### S05 내 기록 상세

- `GET /diaries/{diaryId}` → `{ diaryId, diaryDate, title, content, language, photos[], correctionProgress }`
  - `photos[]: { sortOrder, url, positionX, positionY, scale, rotation }`
- [삭제] → `DELETE /diaries/{diaryId}` → S09로 이동. 사진·교정까지 삭제되고 같은 날 다시 쓸 수 있다.
- 실패: 404 `DIARY_NOT_FOUND`(없거나 내 기록이 아님)

### S06 탐색

- `GET /public-diaries?language=ja&size=20` (`language`를 빼면 [전체])
- 응답: `diaries[]`, `nextCursor`
  - `diaries[]: { diaryId, diaryDate, title, language, firstPhotoUrl, author, correctionProgress }`
  - `author: { userId, nickname, profileImageUrl, nationality, learningLanguage, learningLevel }`
- [더 보기] → 같은 조건에 `cursor=<nextCursor>`. `nextCursor`가 `null`이면 마지막 페이지
- 당겨서 새로고침 → `cursor` 없이 다시 조회
- 카드 탭 → S07-1, 작성자 탭 → S10

### S07-1 공개 기록 상세

- `GET /public-diaries/{diaryId}`
- 응답: `{ …, photos[], author, correctionProgress, mine, correctable, correctedByMe }`
- 버튼 결정:
  - `correctedByMe=true` → [교정완료](비활성)
  - `correctable=true` → [교정하기] 활성 → S07-2
  - 그 외 → 버튼 숨김 또는 비활성
- 404 `DIARY_NOT_FOUND` → "더 이상 볼 수 없어요"

### S07-2 교정 작성 → S04-4 전송

1. 화면 열 때 `POST /public-diaries/{diaryId}/correction-claim` → `{ diaryId, title, author, claimExpiresAt, sentences[] }`
   - `sentences[]: { sentenceId, sentenceIndex, content }`
   - 맡은 시간은 30분(`claimExpiresAt`)이다. 남은 시간을 보여주거나, 다시 들어오면 같은 API로 연장된다.
   - 409 `CORRECTION_ALREADY_CLAIMED` → "다른 사람이 먼저 교정하고 있어요" + [다시 시도], 409 `CORRECTION_ALREADY_COMPLETED`, 403 `CORRECTION_OWN_DIARY`
2. [제출] → `POST /public-diaries/{diaryId}/corrections` (201)

```json
{ "items": [
  { "sentenceId": 11, "changed": true, "correctedContent": "今日は晴れでした。", "reason": "과거형" },
  { "sentenceId": 12, "changed": false }
], "comment": "잘 썼어요!" }
```

   - **모든 문장**을 한 번씩 보낸다. `changed=true`면 `correctedContent` 필수, `reason`은 선택. `comment` 필수(1000자 이내)
   - 응답: `{ correctionId, diaryId, authorProfileImageUrl }` → S04-4(교정) 후 S07-1로 이동
   - 실패: 400 `CORRECTION_INCOMPLETE_ITEMS`, `CORRECTION_INVALID_ITEM`, 409 `CORRECTION_CLAIM_NOT_ACTIVE`(30분 지남 → 다시 맡기)
3. 제출하지 않고 나갈 때(선택) → `DELETE /public-diaries/{diaryId}/correction-claim`. 호출하지 않아도 30분 뒤 자동으로 풀린다.
- 작성 중 초안은 기기에만 저장한다.

### S08 교정 결과

1. `GET /diaries/{diaryId}/correction`
   - 응답: `{ correctionId, corrector, comment, correctedAt, readAt, items[] }`
   - `corrector: { userId, nickname, profileImageUrl }`
   - `items[]: { sentenceId, sentenceIndex, original, changed, correctedContent, reason }`
2. 화면에 들어오면 함께 `POST /diaries/{diaryId}/correction/read`를 호출해 홈의 빨간 점을 지운다.
- 404 `CORRECTION_NOT_FOUND`(아직 교정 전)

### S09 이전 기록·달력

- `GET /diaries?year=2026&month=11` → `diaries[]: { diaryId, diaryDate, title, firstPhotoUrl, correctionProgress }`
- 날짜 칸: `firstPhotoUrl` 표시, `COMPLETED`는 분홍 테두리
  - `COMPLETED`이면 탭 → S08
  - 그 외 → S05

### S10 사용자 프로필

- `GET /users/{userId}/profile` → `{ userId, nickname, profileImageUrl, nationality, nativeLanguage, learningLanguage, learningLevel, age, gender }`
  - `gender`는 공개한 경우에만 값이 있고, 아니면 `null`
  - 생년월일 원본과 이메일은 오지 않는다.
- `GET /users/{userId}/public-diaries?size=20` → S06과 같은 카드 형식
- 404 `PROFILE_NOT_FOUND`

### S13-1 설정

| 항목 | API |
| --- | --- |
| 계정 이메일 표시 | `GET /users/me/profile`의 `email`(Apple 이메일 숨김이면 `null`) |
| [8] 약관 및 동의 관리 | `GET /users/me/consents` → 약관별 `agreed`, `consentedAt`. 선택 약관 변경은 `POST /users/me/consents`에 그 약관만 보낸다. 필수 약관은 철회할 수 없다. |
| [9] 로그아웃 | `POST /auth/logout` `{ "refreshToken": "…" }` → 기기의 토큰 삭제 → S01-2 |
| [10] 회원탈퇴 | 확인창 2번 후 `DELETE /users/me` → 토큰 삭제 → S01-2 |

### S13-5 내 첨삭 활동

- `GET /users/me/corrections?size=20`
- 응답: `corrections[]`, `nextCursor`
  - `corrections[]: { correctionId, diaryId, diaryDate, excerpt, comment, correctedAt }`

## 4. 화면별 API 요약

| 화면 | API |
| --- | --- |
| S01-1 스플래시 | `GET /users/me/registration` |
| S01-2 로그인 | `POST /auth/apple`, `POST /auth/google` |
| S02-0 약관 | `GET /terms`, `GET /terms/{id}`, `POST /users/me/consents` |
| S02-1·S13-3 프로필 | `POST /uploads/profile-image`, `PUT /users/me/profile`, `GET /users/me/profile` |
| S03 홈 | `GET /home` |
| S04-1 새 기록 | `GET /diaries/today`, `POST /uploads/diary-images`, `POST /diaries` |
| S05 내 기록 | `GET /diaries/{id}`, `DELETE /diaries/{id}` |
| S06 탐색 | `GET /public-diaries` |
| S07-1 공개 기록 | `GET /public-diaries/{id}` |
| S07-2 교정 작성 | `POST`·`DELETE /public-diaries/{id}/correction-claim`, `POST /public-diaries/{id}/corrections` |
| S08 교정 결과 | `GET /diaries/{id}/correction`, `POST /diaries/{id}/correction/read` |
| S09 달력 | `GET /diaries?year=&month=` |
| S10 사용자 프로필 | `GET /users/{id}/profile`, `GET /users/{id}/public-diaries` |
| S13-1 설정 | `GET /users/me/consents`, `POST /auth/logout`, `DELETE /users/me` |
| S13-5 첨삭 활동 | `GET /users/me/corrections` |
| 공통 | `POST /auth/reissue` |

## 5. 사진 업로드 흐름

1. 서버에 업로드 URL을 요청한다.
   - 프로필: `POST /uploads/profile-image`
   - 일기: `POST /uploads/diary-images { count }`
2. 받은 `uploadUrl`로 파일을 **직접** 올린다. `PUT`, 본문은 이미지 파일, `Content-Type`은 이미지 형식(예: `image/jpeg`). 서버 토큰은 붙이지 않는다.
3. 업로드가 성공하면 받은 `path`를 프로필 저장(`profileImagePath`)이나 일기 작성(`photos[].path`)에 넣는다.
4. 화면에 보여줄 때는 응답의 `profileImageUrl`, `url`, `firstPhotoUrl`을 그대로 쓴다.
   - 일기 사진 URL은 1시간 뒤 만료되는 서명 URL이라 캐시 키로 쓰지 않는다. 화면을 다시 열면 새 URL을 받는다.
- 사진은 앱에서 먼저 줄인다(긴 변 1600px, JPEG). 기존 정책 D05 기준이다.
- 업로드 URL은 발급 후 2시간 안에 써야 한다. 저장 전에 사진을 바꾸면 새로 발급한다.

## 6. 기존 Supabase 코드에서 바꿀 것

| 지금 | 바뀌는 방식 |
| --- | --- |
| `supabase.auth.signInWithIdToken`(Apple) | `POST /auth/apple`, 받은 서버 토큰 저장 |
| `supabase.auth.signInWithOAuth`(Google 웹) | 네이티브 Google 로그인 → `POST /auth/google` |
| `supabase.auth` 세션·`signOut` | 서버 토큰(SecureStore) + `POST /auth/reissue`·`POST /auth/logout` |
| `rpc('save_profile_stage')`, `storage.from('avatars')` | `POST /uploads/profile-image` → 업로드 → `PUT /users/me/profile` |
| `rpc('save_diary_with_media')`, `rpc('finalize_diary_media')`, `request_human_correction` | `POST /uploads/diary-images` → 업로드 → `POST /diaries` |
| `rpc('list_public_diaries')`, `rpc('get_public_diary')` | `GET /public-diaries`, `GET /public-diaries/{id}` |
| `rpc('claim_correction_request')`, `release_correction_claim`, `complete_correction` | `POST`·`DELETE …/correction-claim`, `POST …/corrections` |
| `rpc('claim_daily_token')`(프로필 조회) | `GET /home`, `GET /users/me/profile`. 우표는 MVP에서 없다. |
| `functions.invoke('delete-account')` | `DELETE /users/me` |

- 클라이언트의 `src/features/*/repository.ts`가 바꿀 위치다. 화면 컴포넌트는 repository 함수의 반환 형태만 맞추면 된다.
- 앱의 `.env.local`에서 Supabase URL·publishable 키는 더 이상 필요 없고, 서버 주소(예: `EXPO_PUBLIC_API_BASE_URL`)를 추가한다.
