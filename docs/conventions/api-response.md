# 응답과 오류 처리

성공 응답, 예외, 오류 코드나 오류 응답을 만들거나 바꿀 때 읽는다. 공통 클래스는 `global/response`와 `global/exception`에 있으며, 구현을 바꾸면 이 문서도 함께 갱신한다.

## 성공 응답

모든 정상 응답은 `global/response/BaseResponse`로 감싼다. `status`는 실제 HTTP 상태 코드와 같게 둔다.

- 200 응답은 `BaseResponse.ok(message, data)`, 데이터가 없으면 `BaseResponse.ok(message)`를 쓴다.
- 201처럼 다른 상태 코드는 `BaseResponse.of(HttpStatus.CREATED, message, data)`로 만들고 `ResponseEntity`의 상태 코드와 맞춘다.

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

- `ErrorDto.of(errorCode, path)`로 만들며 `timestamp`는 UTC ISO-8601 문자열이다.
- 인증 실패(401)·권한 없음(403)은 Security 단계에서 `CustomAuthenticationEntryPoint`·`CustomAccessDeniedHandler`가 같은 `ErrorDto` 형식으로 응답한다.

## 오류 코드

- 오류 코드는 `global/exception/ErrorCode` 인터페이스(`status`, `code`, `message`)를 구현한 enum으로 만든다.
- 여러 도메인이 함께 쓰는 오류는 `CommonErrorCode`, 도메인 오류는 `domain/{기능}/exception/{Domain}ErrorCode`에 둔다.
- `code`는 `{도메인}_{오류}` 형식의 대문자 문자열이다. 예: `COMMON_INVALID_INPUT`, `DIARY_NOT_FOUND`.
- `message`는 사용자가 읽을 수 있는 한국어 문장으로 쓴다.

| CommonErrorCode | 상태 | 쓰는 때 |
| --- | --- | --- |
| `INVALID_INPUT` | 400 | Bean Validation 실패, 파라미터 누락·형식 오류 |
| `INVALID_REQUEST_BODY` | 400 | JSON 본문을 읽을 수 없음 |
| `UNAUTHORIZED` | 401 | 인증 정보 없음·유효하지 않음 |
| `FORBIDDEN` | 403 | 권한 없음 |
| `RESOURCE_NOT_FOUND` | 404 | 없는 경로 |
| `METHOD_NOT_ALLOWED` | 405 | 지원하지 않는 HTTP 메서드 |
| `INTERNAL_SERVER_ERROR` | 500 | 예상하지 못한 오류 |

## 예외 처리

- 예상 가능한 비즈니스 실패는 `throw new CustomException(DiaryErrorCode.DIARY_NOT_FOUND)`처럼 도메인의 `{Domain}ErrorCode`와 `CustomException`으로 표현하고, `GlobalExceptionHandler`가 `ErrorDto`로 바꾼다.
- Bean Validation 실패는 첫 번째 필드 오류의 메시지를 `INVALID_INPUT` 응답의 `message`로 내려준다.
- 입력 형식은 DTO의 Bean Validation으로, 상태·소유권은 Service 또는 Entity에서 검사한다.
- 처리 가능한 비즈니스 오류는 알맞은 4xx로 응답하고 500으로 뭉개지 않는다.
- 예상하지 못한 오류는 서버 로그에 남기고, 클라이언트에는 내부 예외 메시지·스택·비밀값을 노출하지 않는다.
- 로그에는 오류 코드·경로처럼 진단에 필요한 정보만 남긴다. 토큰·비밀번호·개인정보나 요청 본문 전체를 기록하지 않는다.
