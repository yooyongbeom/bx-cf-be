# BX-CF-BE (Channel Backend Framework)

BWG 채널 백엔드 공통 프레임워크 프로젝트. MSA 기반의 인증/인가 및 API 게이트웨이 기능 제공.

---

## 📁 프로젝트 구조

```
bx-cf-be/
├── libs/
│   ├── common/       공통 응답/예외/AOP/JPA·MyBatis 기반 설정 라이브러리
│   └── auth-core/    JWT 인증 필터 및 공통 인증 라이브러리 (AuthErrorCode, BwgAuthException)
├── infra/
│   ├── discovery-svc/  서비스 등록 및 발견 (Eureka Server)
│   └── api-gateway/    라우팅 및 보안 필터링 (Spring Cloud Gateway)
└── services/
    ├── auth-svc/     사용자 인증 및 토큰 발급 (JPA + MyBatis 듀얼 데이터소스)
    └── product-svc/  상품 관리 (JPA + MyBatis 듀얼 데이터소스)
```

---

## 🏗 공통 라이브러리 구조 (libs/common)

| 패키지 | 설명 |
| :--- | :--- |
| `common.constants.enums` | `BwgErrorCode` 인터페이스 — 서비스별 에러코드 enum 공통 계약 |
| `common.exception` | `BwgException` — 서비스별 예외 클래스의 최상위 기반 |
| `common.aop` | `BwgExceptionAspect` — 전 서비스 공통 예외 래핑 AOP |
| `common.aop` | `BwgAuthExceptionAdvice` — `@RestControllerAdvice` 공통 에러 응답 처리 |
| `common.configuration` | `JpaMainConfigBase`, `MyBatisMainConfigBase` — 서비스별 DB 설정 추상 기반 클래스 |

> 서비스별 예외/에러코드(예: `AuthErrorCode`, `ProductErrorCode`)는 각 서비스 모듈에 위치합니다.

---

## 🚀 서비스 구성 및 실행 순서

로컬/개발 환경 구동 시 다음 순서대로 서비스 실행 권장.

1. **Discovery Service (`discovery-svc`)** — 서비스 등록 및 발견 (Eureka) : Port `18761`
2. **Auth Service (`auth-svc`)** — 사용자 인증 및 토큰 발급 : Port `18082`
3. **Product Service (`product-svc`)** — 상품 관리 : Port `18083`
4. **API Gateway (`api-gateway`)** — 라우팅 및 보안 필터링 : Port `18081`

---

## 🔗 주요 접속 정보

### Local
- **Eureka Dashboard**: [http://localhost:18761/](http://localhost:18761/)
- **DB**: PostgreSQL `jdbc:postgresql://192.168.11.197:5432/bxcfdb`
- **Postman Collection**: `reffile/BX-CF-BE-LCL.postman_collection.json` 참조

### Dev
- **Eureka Dashboard**: [http://192.168.11.197:18761/](http://192.168.11.197:18761/)
- **DB**: PostgreSQL `jdbc:postgresql://192.168.11.197:5432/bxcfdb`

---

## 📦 빌드 및 실행

```bash
# dev 프로파일로 JAR 빌드 (4개 서비스 한번에)
./gradlew :infra:discovery-svc:bootJar :infra:api-gateway:bootJar \
          :services:auth-svc:bootJar :services:product-svc:bootJar -Pprofile=dev

# JAR 실행 예시
java -jar -Dfile.encoding=UTF-8 discovery-svc-0.0.1-SNAPSHOT.jar
java -jar -Dfile.encoding=UTF-8 auth-svc-0.0.1-SNAPSHOT.jar
java -jar -Dfile.encoding=UTF-8 product-svc-0.0.1-SNAPSHOT.jar
java -jar -Dfile.encoding=UTF-8 api-gateway-0.0.1-SNAPSHOT.jar
```

> 빌드 결과물 위치: `{모듈}/build/libs/*.jar`

---

## 🛠 공통 응답 규격 (Common Response Format)

모든 API 응답은 아래와 같은 공통 구조를 가짐.

```json
{
  "success": true,
  "code": "0",
  "msg": "success",
  "payload": { ... }
}
```

---

## ⚠️ 에러 코드 정의 (Error Code Specifications)

### 1. 인증/인가 에러 (AuthErrorCode) — `libs/auth-core`

| 코드 | 상수명 | 설명 |
| :--- | :--- | :--- |
| `-1001` | `REQUIRED_VALUE_MISSING` | 필수 입력값 누락 |
| `-1002` | `INVALID_TOKEN` | 유효하지 않은 토큰 |
| `-1003` | `UNAUTHORIZED_CLIENT` | 인증되지 않은 클라이언트 |
| `-1004` | `EXPIRED_TOKEN` | 토큰 유효시간 만료 (Refresh 필요) |
| `-1005` | `ACCESS_DENIED` | 해당 리소스 접근 권한 없음 |
| `-2003` | `JSON_STR_TO_VO_PARSING_ERROR` | JSON 문자열 파싱 오류 |
| `-2004` | `JSON_VO_TO_STR_PARSING_ERROR` | VO 객체 JSON 변환 오류 |
| `-4001` | `DB_NO_DATA_ERROR` | DB 데이터 조회 실패 |
| `-4002` | `DB_SAVE_DATA_ERROR` | DB 데이터 저장 오류 |
| `-9999` | `SERVER_ERROR` | 서버 내부 시스템 오류 |

### 2. 게이트웨이 에러 (GatewayErrorCode) — `infra/api-gateway`

| 코드 | 상수명 | 설명 |
| :--- | :--- | :--- |
| `-9999` | `SERVER_ERROR` | 게이트웨이 서버 내부 오류 |

### 3. 상품 에러 (ProductErrorCode) — `services/product-svc`

| 코드 | 상수명 | 설명 |
| :--- | :--- | :--- |
| `-5001` | `REQUIRED_VALUE_MISSING` | 필수값 누락 |
| `-5002` | `INVALID_PRODUCT_ID` | 유효하지 않은 상품 ID |
| `-5003` | `INVALID_PRICE` | 가격은 0 이상이어야 함 |
| `-5004` | `INVALID_STOCK_QTY` | 재고 수량은 0 이상이어야 함 |
| `-5101` | `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없음 |
| `-5102` | `PRODUCT_LIST_EMPTY` | 조회된 상품 없음 |
| `-5201` | `PRODUCT_SAVE_ERROR` | 상품 저장 실패 |
| `-5202` | `PRODUCT_UPDATE_ERROR` | 상품 수정 실패 |
| `-5203` | `PRODUCT_DELETE_ERROR` | 상품 삭제 실패 |
| `-5301` | `PRODUCT_INACTIVE` | 비활성화된 상품 |
| `-5302` | `OUT_OF_STOCK` | 재고 부족 |
| `-5999` | `SERVER_ERROR` | 상품 서비스 내부 오류 |

---

## 📦 상품 서비스 API (product-svc)

> Gateway 경유 기준 URL: `http://도메인:18081/channel/backend/api/v1/product`

### 1. 상품 목록 조회

- **Endpoint**: `GET /channel/backend/api/v1/product/list`
- **Query Parameters** (선택)

| 파라미터 | 타입 | 설명 |
| :--- | :--- | :--- |
| `productNm` | String | 상품명 (부분 검색) |
| `useYn` | String | 사용 여부 (`Y` / `N`) |

- **Request 예시**
```
GET /channel/backend/api/v1/product/list?useYn=Y&productNm=펀드
```

- **Response 예시**
```json
{
  "success": true,
  "code": "0",
  "msg": "success",
  "payload": [
    {
      "productId": 1,
      "productNm": "KB 적립식 펀드",
      "productDesc": "KB자산운용 국내 주식형 적립식 펀드 상품",
      "price": 100000,
      "stockQty": 999,
      "useYn": "Y"
    }
  ]
}
```

### 2. 상품 단건 조회

- **Endpoint**: `GET /channel/backend/api/v1/product/{productId}`

- **Request 예시**
```
GET /channel/backend/api/v1/product/1
```

- **Response 예시**
```json
{
  "success": true,
  "code": "0",
  "msg": "success",
  "payload": {
    "productId": 1,
    "productNm": "KB 적립식 펀드",
    "productDesc": "KB자산운용 국내 주식형 적립식 펀드 상품",
    "price": 100000,
    "stockQty": 999,
    "useYn": "Y"
  }
}
```

- **상품 없을 경우 Response 예시**
```json
{
  "success": false,
  "code": "-5101",
  "msg": "상품을 찾을 수 없습니다",
  "payload": null
}
```

---

## 🔐 인증 및 토큰 재발급 프로세스

### 1. 로그인 (Login)
- **Endpoint**: `POST /channel/backend/api/v1/auth/login`
- **Body**: `{ "usrId": "ID", "usrPwd": "SHA-256-PWD" }`

### 2. 토큰 검증 및 재발급 흐름
1. 클라이언트는 API 요청 시 `Authorization: Bearer <AccessToken>` 헤더 포함.
2. Gateway 및 Auth Filter에서 Access Token 검증.
3. **Token 만료 시**: 서버는 `-1004 (EXPIRED_TOKEN)` 코드 반환.
4. **클라이언트 대응**: `-1004` 에러 수신 시, Refresh Token으로 재발급 API 호출.
   - **Endpoint**: `POST /channel/backend/api/v1/auth/refresh-token`
   - **Body**: `{ "refreshToken": "..." }`
5. **성공 시**: 새로운 Access/Refresh Token 발급받아 기존 요청 재시도.

---

## 📄 참고 문서
- 상세 아키텍처 및 프로세스 설명: `reffile/웹앱 프레임워크_구축 v0.5 2025.09.19.pptx`
