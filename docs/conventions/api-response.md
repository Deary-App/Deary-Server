# 응답과 오류 처리

성공 응답, 예외, 오류 코드나 오류 응답을 만들거나 바꿀 때 읽는다. 아직 공통 응답·예외 클래스가 없으므로 처음 만들 때 아래 형식을 따르고, 만든 뒤에는 실제 구현을 기준으로 이 문서를 갱신한다.

## 성공 응답

모든 정상 응답은 `BaseResponse` 형식으로 감싼다. `status`는 실제 HTTP 상태 코드와 같게 둔다.

```json
{
  "status": 200,
  "message": "일기 조회에 성공했습니다.",
  "data": {}
}
```

## 오류 응답

오류는 `global/exception/dto/ErrorDto` 형식으로 응답한다. 정상 응답의 `BaseResponse`와 형식이 다르다는 점을 유지한다.

```java
public record ErrorDto(
        String timestamp,
        int status,
        String errorCode,
        String message,
        String path
) {}
```

## 예외 처리

- 예상 가능한 비즈니스 실패는 도메인의 `{Domain}ErrorCode`와 공통 예외로 표현하고, 전역 예외 핸들러가 `ErrorDto`로 바꾼다.
- 입력 형식은 DTO의 Bean Validation으로, 상태·소유권은 Service 또는 Entity에서 검사한다.
- 처리 가능한 비즈니스 오류는 알맞은 4xx로 응답하고 500으로 뭉개지 않는다.
- 예상하지 못한 오류는 서버 로그에 남기고, 클라이언트에는 내부 예외 메시지·스택·비밀값을 노출하지 않는다.
- 로그에는 오류 코드·경로처럼 진단에 필요한 정보만 남긴다. 토큰·비밀번호·개인정보나 요청 본문 전체를 기록하지 않는다.
