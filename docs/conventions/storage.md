# 사진 저장소 (Supabase Storage)

사진 업로드·조회·삭제를 다룰 때 읽는다.

## 흐름

1. 앱이 업로드 URL 발급 API를 호출하면 서버가 저장 경로를 정하고 Supabase Storage의 서명된 업로드 URL을 받아 `path`, `uploadUrl`로 돌려준다.
2. 앱은 `uploadUrl`로 파일을 직접 올린다(`PUT`, 파일 본문, `Content-Type` 지정). 파일은 서버를 거치지 않는다.
3. 앱이 저장 API(프로필 저장 등)에 `path`를 보내면 서버는 그 경로가 요청한 사용자 것인지 확인한 뒤 저장한다.
4. 조회 응답에는 보기용 URL을 함께 내려준다.

## 경로와 버킷

| 용도 | 버킷 (환경변수) | 경로 | 보기 URL |
| --- | --- | --- | --- |
| 프로필 사진 | `SUPABASE_PROFILE_BUCKET` (기본 `avatars`, 공개 버킷) | `profiles/{userId}/{UUID}` | 공개 URL |
| 일기 사진 | `SUPABASE_DIARY_BUCKET` (기본 `diary-images`, 비공개 버킷) | 일기 기능에서 정한다 | 서명된 URL (`SUPABASE_DOWNLOAD_URL_EXPIRATION`, 기본 1시간) |

- 경로는 서버가 만들고, 클라이언트가 보낸 경로는 `StoragePathPolicy`로 소유자 폴더와 형식을 확인한다. 다른 사용자 폴더나 `..`가 들어간 경로는 거절한다.
- 파일 크기·형식 제한은 Supabase 버킷 설정(허용 MIME, 최대 크기)으로 건다.
- 회원탈퇴 시 사용자 폴더의 파일을 삭제한다. 삭제 실패는 로그만 남기고 탈퇴는 계속 진행한다.

## 설정과 보안

- `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`는 환경변수로만 받는다. 서비스 관리자 키는 서버에만 두고 앱·로그·응답에 내보내지 않는다.
- 설정이 비어 있으면 업로드 URL 발급은 `STORAGE_UNAVAILABLE`(503)로 응답하고, 보기 URL은 `null`이다.
- Storage 요청 실패 로그에는 URL·토큰을 남기지 않고 동작 이름과 예외 종류만 남긴다.
