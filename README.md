# BX-CF-BE

BWG 채널 백엔드 프레임워크. Spring Boot 3.3 / Java 21 / MSA 구조.

## 프로젝트 구조

```
bx-cf-be/
├── libs/
│   ├── common/       공통 예외/AOP/JPA·MyBatis 기반 설정
│   └── auth-core/    JWT 인증 필터 (AuthErrorCode, BwgAuthException)
├── infra/
│   ├── discovery-svc/  Eureka Server (18761)
│   └── api-gateway/    Spring Cloud Gateway (18081)
└── services/
    ├── auth-svc/     인증/토큰 발급 (18082)
    └── product-svc/  상품 관리 (18083)
```

서비스별 예외/에러코드는 각 모듈에 위치. common에는 Bwg 프레임워크 기반 코드만.

## 실행 순서

discovery-svc → auth-svc + product-svc → api-gateway

## 접속 정보

| 환경 | Eureka | DB |
| --- | --- | --- |
| local | http://localhost:18761 | jdbc:postgresql://192.168.11.197:5432/bxcfdb |
| dev | http://192.168.11.197:18761 | jdbc:postgresql://192.168.11.197:5432/bxcfdb |

Postman: `reffile/BX-CF-BE-LCL.postman_collection.json`

## 빌드

```bash
# IntelliJ Gradle Run Configuration
# Tasks: :infra:discovery-svc:bootJar :infra:api-gateway:bootJar :services:auth-svc:bootJar :services:product-svc:bootJar
# Arguments: -Pprofile=dev

# 결과물: build/dist/bx-cf-be/*.jar
```

## 실행 (서버)

```bash
java -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Xms512m -Xmx1024m \
     -Dspring.profiles.active=dev \
     -jar 서비스명.jar
```

## 공통 응답 규격

```json
{ "success": true, "code": "0", "msg": "success", "payload": { } }
```

## API

### 인증

```
POST /channel/backend/api/v1/auth/login
     body: { "usrId": "ID", "usrPwd": "SHA-256-PWD" }

POST /channel/backend/api/v1/auth/refresh-token
     body: { "refreshToken": "..." }
```

토큰 만료 시 `-1004` 반환 → refresh-token으로 재발급 후 재시도.

### 상품

```
GET /channel/backend/api/v1/product/list?useYn=Y&productNm=펀드
GET /channel/backend/api/v1/product/{productId}
```

## 에러 코드

### AuthErrorCode (libs/auth-core)

| 코드 | 설명 |
| --- | --- |
| -1001 | 필수 입력값 누락 |
| -1002 | 유효하지 않은 토큰 |
| -1003 | 인증되지 않은 클라이언트 |
| -1004 | 토큰 만료 |
| -1005 | 접근 권한 없음 |
| -2003 | JSON 파싱 오류 |
| -4001 | DB 조회 실패 |
| -4002 | DB 저장 오류 |
| -9999 | 서버 내부 오류 |

### ProductErrorCode (services/product-svc)

| 코드 | 설명 |
| --- | --- |
| -5001 | 필수값 누락 |
| -5002 | 유효하지 않은 상품 ID |
| -5101 | 상품 없음 |
| -5201 | 상품 저장 실패 |
| -5301 | 비활성화된 상품 |
| -5302 | 재고 부족 |
| -5999 | 상품 서비스 내부 오류 |

### GatewayErrorCode (infra/api-gateway)

| 코드 | 설명 |
| --- | --- |
| -9999 | 게이트웨이 내부 오류 |

## 참고

`reffile/웹앱 프레임워크_구축 v0.5 2025.09.19.pptx`
