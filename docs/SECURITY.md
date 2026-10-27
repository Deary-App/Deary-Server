# 보안 점검

기준: 2026-10-25. 저장소 비밀정보 점검과 서비스 보안 검증을 구분한다.

## 공유 범위

환경파일, 관리자·AI 키, 인증서, 개인 문서, 설치물과 빌드 결과는 Git에서 제외한다. 앱에는 Supabase URL과 공개 publishable key만 사용하며 관리자 권한과 외부 AI 호출은 서버에 둔다. 공개 키의 안전성은 DB RLS와 Storage 접근권한에 의존한다.

`node scripts/security-source-audit.cjs`로 Git 대상 파일을 검사한다. Gitleaks로 작업 파일과 전체 Git 이력도 확인한다. `.gitleaks.toml`의 예외는 과거 증거 manifest의 SHA256 해시 줄에만 적용한다. 파일 전체나 일반 키 패턴을 예외 처리하지 않는다.

## 의존성

클라이언트 `npm audit` 결과는 8건(critical 0, high 5, moderate 3), 서버 검사 도구는 0건이다. 이는 영향받는 의존성 경로 수이며 서로 다른 취약점 8개를 뜻하지 않는다.

| 경로 | 후속 조치 |
|---|---|
| Expo CLI → node-forge | 호환되는 Expo 및 상위 의존성 수정 버전 확인 |
| Expo CLI 내부 Zod 3 | Expo 의존성 갱신 대기. 앱의 직접 Zod 의존성은 갱신됨 |
| Expo Router → query-string → decode-uri-component | Router 호환성 검증을 포함해 갱신. 수정 버전의 ESM 전환 때문에 강제 override하지 않음 |

`npm audit fix --force`는 SDK/Router 호환성을 깨뜨릴 수 있어 적용하지 않았다. 현재 점검은 취약점이 모두 해소됐다는 의미가 아니다. 의존성 갱신 시 앱 시작·화면 이동·빌드를 함께 확인한다.

## 권한과 운영

DB 권한·동시성 검사와 AI/사진 함수 검사는 [검증 현황](VERIFICATION.md)을 따른다. 접근권한 설계는 [서버 권한](../server/docs/PERMISSIONS.md)에 있다. 폴더 정리 과정에서 운영 DB 변경, 서버 배포, 유료 AI 실행은 하지 않는다.
