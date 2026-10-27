# 검증 현황

기준: 2026-10-25 폴더 정리. 앱·서버 실행 코드 159개 중 158개와 DB migration 26개의 내용은 이동 전과 동일하다. 변경한 실행 코드는 환경값 누락 안내의 문서 경로 한 줄이다. 검사 위치·실행 명령·문서는 새 경로에 맞췄다.

| 검사 | 결과 |
|---|---|
| 별도 임시 폴더에서 client/server npm ci | 통과 |
| client TypeScript·네이티브 설정 | 통과 |
| client 오프라인 검사 | 14개 통과 |
| server AI·사진 함수 검사 | 35개 통과 |
| 격리 PostgreSQL | 26개 migration 적용, 권한·정책·동시성 검사 통과 |
| WebKit 교정·사진 검사 | 통과 |
| Expo iOS export | 이동 후 통과. 이후 환경값 누락 안내의 문서 경로만 수정 |
| CocoaPods deployment 설치 | 통과 |
| iOS Simulator 네이티브 빌드 | Xcode 26.2, Debug arm64, 서명 없이 빌드 통과 |

클라이언트는 `cd client` 후 `npm run typecheck`, `npm test`, `npm run check:native`로 검사한다. 서버는 `cd server` 후 `npm test`, `npm run test:db`로 검사한다. 추가 환경과 통합 검사는 각 README를 따른다.

이번 정리는 로컬 검사다. 원격 HTTP 회귀의 기존 115개 통과 기록을 이번에 다시 실행한 결과로 간주하지 않는다. 실제 iPhone/iPad 설치·로그인·사진·필기 왕복은 [실기기 체크리스트](ACCEPTANCE.md)로 별도 검증해야 한다.

과거 보고서·원본 자료·이동 전 Git bundle은 저장소 밖 `Deary-local-archive/2026-10-25-folder-cleanup/`에 보관했다. 기존 Git 이력은 유지했다.
