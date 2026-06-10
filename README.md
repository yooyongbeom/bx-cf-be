# BX-CF-BE (Channel Backend Framework)

BWG 채널 백엔드 공통 프레임워크 프로젝트. MSA 기반의 인증/인가 및 API 게이트웨이 기능 제공.

---

## 🚀 서비스 구성 및 실행 순서

로컬 환경 구동 시 다음 순서대로 서비스 실행 권장.

1.  **Discovery Service (`discovery-svc`)**: 서비스 등록 및 발견 (Eureka) - Port: 18761
2.  **Auth Service (`auth-svc`)**: 사용자 인증 및 토큰 발급 - Port: 18081 (Internal: 18082)
3.  **API Gateway (`api-gateway`)**: 라우팅 및 보안 필터링 - Port: 18081

---

## 🔗 주요 접속 정보 (Local)

-   **Eureka Dashboard**: [http://localhost:18761/](http://localhost:18761/)
-   **H2 Database Console (Auth)**: [http://localhost:18082/auth/h2-console](http://localhost:18082/auth/h2-console)
    -   JDBC URL: `jdbc:h2:mem:auth-db`
-   **Postman Collection**: `reffile/BX-CF-BE-LCL.postman_collection.json` 참조

---

## 🛠 공통 응답 규격 (Common Response Format)

모든 API 응답은 아래와 같은 공통 구조를 가짐.

```json
{
  "success": true,        // 성공 여부 (true/false)
  "code": "0",           // 결과 코드 (성공: "0", 실패: 음수)
  "msg": "success",      // 결과 메시지
  "payload": { ... }     // 실제 데이터 (객체 또는 배열)
}
```

---

## ⚠️ 에러 코드 정의 (Error Code Specifications)

시스템 주요 에러 코드 정의.

### 1. 인증/인가 에러 (AuthErrorCode)

| 코드 | 상수명 | 설명 |
| :--- | :--- | :--- |
| `-1001` | `REQUIRED_VALUE_MISSING` | 필수 입력값 누락. |
| `-1002` | `INVALID_TOKEN` | 유효하지 않은 토큰. |
| `-1003` | `UNAUTHORIZED_CLIENT` | 인증되지 않은 클라이언트. |
| `-1004` | `EXPIRED_TOKEN` | 토큰 유효시간 만료. (Refresh 필요) |
| `-1005` | `ACCESS_DENIED` | 해당 리소스 접근 권한 없음. |
| `-2003` | `JSON_STR_TO_VO_PARSING` | JSON 문자열 파싱 오류. |
| `-2004` | `JSON_VO_TO_STR_PARSING` | VO 객체 JSON 변환 오류. |
| `-4001` | `DB_NO_DATA_ERROR` | DB 데이터 조회 실패. |
| `-4002` | `DB_SAVE_DATA_ERROR` | DB 데이터 저장 오류. |
| `-9999` | `SERVER_ERROR` | 서버 내부 시스템 오류. |

### 2. 게이트웨이 에러 (GatewayErrorCode)

| 코드 | 상수명 | 설명 |
| :--- | :--- | :--- |
| `-9999` | `SERVER_ERROR` | 게이트웨이 서버 내부 오류. |

---

## 🔐 인증 및 토큰 재발급 프로세스

### 1. 로그인 (Login)
-   **Endpoint**: `POST /channel/backend/api/v1/auth/login`
-   **Body**: `{ "usrId": "ID", "usrPwd": "SHA-256-PWD" }`

### 2. 토큰 검증 및 재발급 흐름
1.  클라이언트는 API 요청 시 `Authorization: Bearer <AccessToken>` 헤더 포함.
2.  Gateway 및 Auth Filter에서 Access Token 검증.
3.  **Token 만료 시**: 서버는 `-1004 (EXPIRED_TOKEN)` 코드 반환.
4.  **클라이언트 대응**: `-1004` 에러 수신 시, Refresh Token으로 재발급 API 호출.
    -   **Endpoint**: `POST /channel/backend/api/v1/auth/refresh-token`
    -   **Body**: `{ "refreshToken": "..." }`
5.  **성공 시**: 새로운 Access/Refresh Token 발급받아 기존 요청 재시도.

---

## 📦 빌드 및 실행

```bash
# JAR 실행 예시
java -jar -Dspring.profiles.active=local -Dfile.encoding=UTF-8 discovery-svc-0.0.1-SNAPSHOT.jar
```

---

## 📄 참고 문서
-   상세 아키텍처 및 프로세스 설명: `reffile/웹앱 프레임워크_구축 v0.5 2025.09.19.pptx`
