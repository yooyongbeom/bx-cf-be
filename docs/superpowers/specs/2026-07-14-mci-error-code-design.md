# MCI 예외 코드 표준화 설계

## 목표

예상 가능한 MCI 요청 검증·거래 라우팅·구성 오류가 공통 미분류 오류인 `-9999`로 응답되지 않도록 MCI 전용 오류 코드와 typed exception을 도입한다.

## 현재 문제

`MciException`은 일반 `RuntimeException`이므로 `GlobalRestExceptionAdvice`의 최종 `Exception` 처리기로 전달된다. 그 결과 요청 헤더 누락, 미등록 거래, 비활성 거래, Adapter/Mapper 구성 누락처럼 원인이 명확한 오류도 모두 `CommonErrorCode.SERVER_ERROR(-9999)`로 응답된다.

`-9999`는 분류되지 않은 서버 오류에만 사용하고, MCI 계층에서 의미를 알고 발생시키는 오류는 MCI 전용 코드로 전달해야 한다.

## 검토한 접근법

### 1. `GlobalRestExceptionAdvice`에 `MciException` 전용 처리기 추가

구현량은 적지만 공통 계층이 MCI 모듈을 의존하게 되어 모듈 방향이 역전된다. MCI 오류가 추가될 때마다 공통 핸들러도 변경해야 하므로 사용하지 않는다.

### 2. `MciException` 메시지를 분석하여 코드 결정

기존 호출부 변경은 적지만 문자열 변경에 따라 응답 코드가 달라지는 취약한 구조다. 컴파일 시점에 누락을 검출할 수 없어 사용하지 않는다.

### 3. `MciErrorCode`와 `BwgException` 기반 typed exception 도입

`mci-common`이 이미 의존하는 `common`의 `BwgErrorCode`와 `BwgException` 계약을 그대로 사용한다. 공통 핸들러 변경 없이 오류 코드, 메시지, HTTP 상태를 일관되게 반환할 수 있으므로 이 방식을 채택한다.

## 오류 코드 체계

저장소에서 사용하지 않는 `-6xxx`를 MCI 대역으로 할당한다.

| 코드 | enum | 의미 | HTTP 상태 |
| --- | --- | --- | --- |
| `-6001` | `REQUEST_HEADER_REQUIRED` | MCI 요청 헤더 누락 | 400 Bad Request |
| `-6002` | `TRANSACTION_CODE_REQUIRED` | 거래 코드 누락 | 400 Bad Request |
| `-6101` | `TRANSACTION_NOT_REGISTERED` | 등록되지 않은 거래 | 404 Not Found |
| `-6102` | `TRANSACTION_DISABLED` | 비활성 거래 | 503 Service Unavailable |
| `-6103` | `CHANNEL_NOT_ALLOWED` | 허용되지 않은 채널 | 403 Forbidden |
| `-6201` | `ADAPTER_NOT_REGISTERED` | Adapter 구성 누락 | 500 Internal Server Error |
| `-6202` | `MAPPER_NOT_REGISTERED` | Mapper 구성 누락 | 500 Internal Server Error |
| `-6999` | `SERVER_ERROR` | 분류된 MCI 내부 오류의 fallback | 500 Internal Server Error |

Adapter 실제 호출의 timeout과 대외 시스템 실패는 현재 SPI에 실패 유형 계약이 없으므로 이번 변경에서 임의로 분류하지 않는다. 향후 Adapter 실패 계약이 생기면 `-63xx` 대역을 별도 설계한다.

## 구성 요소

### `MciErrorCode`

`BwgErrorCode`를 구현하는 enum으로 코드, 기본 응답 메시지, HTTP 상태를 보유한다. 코드 조회가 필요한 경우 기존 모듈 enum과 동일하게 `fromCode(String)`를 제공한다.

### `MciException`

`RuntimeException` 대신 `BwgException`을 상속한다. 생성 시 `MciErrorCode`를 필수로 받고 기본 응답 메시지는 enum의 메시지를 사용한다. 거래 코드, 채널 코드, Adapter/Mapper 이름 같은 실행 문맥은 클라이언트 메시지에 결합하지 않고 `details`에 저장한다.

### `MciRouter`

현재 문자열 메시지로 발생시키는 일곱 개 예외 지점을 각 상황에 맞는 `MciErrorCode`로 치환한다. 필요한 문맥은 `details`에 담고, 클라이언트에는 안정적인 기본 메시지를 반환한다.

### `GlobalRestExceptionAdvice`

변경하지 않는다. `MciException`이 `BwgException`이 되면 기존 `@ExceptionHandler(BwgException.class)`가 MCI 코드와 HTTP 상태를 그대로 응답한다.

## 응답 흐름

1. `MciRouter`가 요청 또는 거래 구성을 검증한다.
2. 실패 상황에 맞는 `MciErrorCode`와 문맥으로 `MciException`을 생성한다.
3. `GlobalRestExceptionAdvice`가 이를 `BwgException`으로 처리한다.
4. 클라이언트는 기존 `ApiResponse` 형식을 유지하면서 MCI 코드와 해당 HTTP 상태를 받는다.

예시 응답은 다음과 같다.

```json
{
  "success": false,
  "code": "-6101",
  "msg": "MCI transaction is not registered",
  "payload": null
}
```

## 테스트 전략

- `MciErrorCode`의 모든 코드가 `-6`으로 시작하고 중복되지 않는지 검증한다.
- 각 코드의 HTTP 상태가 설계와 일치하는지 검증한다.
- `MciException`이 typed code, 기본 메시지, details를 유지하는지 검증한다.
- `MciRouter`의 기존 일곱 개 실패 경로가 올바른 `MciErrorCode`를 발생시키는지 검증한다.
- `mci-common` 전체 테스트와 `mci-svc` 테스트를 실행하여 공통 핸들러 및 서비스 조립 회귀가 없는지 확인한다.

## 문서 변경

README의 에러 코드 범위 표에 MCI `-6001`~`-6999` 대역을 추가한다.

## 완료 조건

- 예상 가능한 MCI 라우팅 오류가 `-9999` 대신 해당 MCI 코드로 응답된다.
- HTTP 상태가 오류 성격과 일치한다.
- 내부 Adapter/Mapper 이름이 클라이언트 응답 메시지에 노출되지 않는다.
- 기존 성공 라우팅 동작과 MCI 서비스 조립이 유지된다.
- 관련 테스트가 모두 통과한다.
