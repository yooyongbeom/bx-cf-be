# BX-CF-BE

BWG 채널 파운데이션(Channel Foundation) 백엔드 프로젝트. Java 21, Spring Boot 3.3.4, Spring Cloud Gateway, Eureka Discovery, Gradle 멀티모듈 기반. 인증, 상품, 기준정보 서비스를 관심사별로 분리.

## 현재 구조

```text
bx-cf-be/
├─ libs/
│  ├─ common/                  공통 응답, 예외 처리, OpenAPI TypeBridge, JPA/MyBatis 기반 설정
│  ├─ business-common/         업무 공통 검증, 업무 예외, 업무 값 객체
│  ├─ mci-common/              MCI 표준 요청/응답, 거래 registry, Adapter/Mapper SPI, Router
│  ├─ security-common/         JWT 발급/검증, 인증 필터, 인증 에러 코드, 내부 인증 헤더
│  └─ session-context-common/  Redis/Valkey 기반 세션 컨텍스트 모델과 저장소
├─ infra/
│  ├─ discovery-svc/           Eureka Server
│  └─ api-gateway/             Spring Cloud Gateway, JWT 검증, 서비스 라우팅
├─ services/
│  ├─ auth-svc/                로그인, 토큰 발급/재발급, 로그아웃, Redis 세션 저장
│  ├─ mci-svc/                 금융 채널 MCI 라우팅, YAML 거래 정의, 샘플 Adapter/Mapper
│  ├─ product-svc/             상품 조회
│  └─ system-svc/              메뉴, 역할별 메뉴, 공통코드, MCI 관리 API 설계
└─ src/main/resources/config/  모듈별 공통 설정과 profile별 설정
```

`settings.gradle` 기준 포함 모듈.

```gradle
libs:common
libs:business-common
libs:mci-common
libs:security-common
libs:session-context-common
infra:api-gateway
infra:discovery-svc
services:auth-svc
services:integration-svc
services:mci-svc
services:product-svc
services:system-svc
```

## 모듈 의존성

| 모듈 | 주요 의존성 | 역할                                                   |
| --- | --- |------------------------------------------------------|
| `infra:api-gateway` | `common`, `security-common`, `session-context-common` | 외부 요청 진입점, JWT 검증, 내부 인증 헤더 생성, Eureka 기반 라우팅        |
| `infra:discovery-svc` | - | 서비스 인스턴스 등록/탐색용 Eureka 서버                            |
| `services:auth-svc` | `common`, `business-common`, `security-common`, `session-context-common` | 사용자 인증, access/refresh token 발급, Redis 세션 컨텍스트 저장/삭제 |
| `services:integration-svc` | `common`, `business-common` | 대외계 연동. GitHub webhook 수신, GitHub 서명 검증, Notion 할일 DB row 생성 |
| `services:mci-svc` | `common`, `business-common`, `mci-common` | 금융 채널 MCI 라우팅, YAML 거래 registry, 고객사 Adapter/Mapper 실행 |
| `services:product-svc` | `common`, `business-common`, `session-context-common` | 상품 API, 필요 시 내부 인증 헤더/sessionId 기반 세션 컨텍스트 조회          |
| `services:system-svc` | `common`, `business-common`, `session-context-common` | 메뉴/공통코드 API, MCI 관리 API 설계, 필요 시 내부 인증 헤더/sessionId 기반 세션 컨텍스트 조회 |
| `libs:mci-common` | `common` | MCI 표준 모델, 거래 정의, Adapter/Mapper SPI, Router 공통 모듈 |
| `libs:security-common` | `common`, Spring Security, JJWT | JWT와 인증 실패 응답 공통화                                    |
| `libs:session-context-common` | `common`, Spring Data Redis | `session:{sessionId}` 규칙의 Redis 세션 컨텍스트 공통화          |

## 실행 순서

로컬에서 여러 서비스를 직접 실행할 때 권장 순서.

1. `infra:discovery-svc`
2. `services:auth-svc`
3. `services:product-svc`
4. `services:system-svc`
5. `services:mci-svc`
6. `services:integration-svc`
7. `infra:api-gateway`

Gateway는 Eureka에 등록된 서비스 이름으로 `lb://...` 라우팅. Gateway보다 하위 서비스와 Discovery를 먼저 기동하는 흐름이 이해하기 쉬움.

## 포트와 경로

| 모듈 | 포트 | 내부 context-path | Gateway 외부 경로 |
| --- | ---: | --- | --- |
| discovery-svc | 18761 | - | - |
| api-gateway | 18081 | - | - |
| auth-svc | 18082 | `/auth` | `/channel/backend/api/v1/auth/**` |
| integration-svc | 18085 | `/integration` | `/channel/backend/api/v1/integration/**` |
| mci-svc | 18086 | `/mci` | `/channel/backend/api/v1/mci/**` |
| product-svc | 18083 | `/product` | `/channel/backend/api/v1/product/**` |
| system-svc | 18084 | `/system` | `/channel/backend/api/v1/system/**` |

환경별 주요 접속 정보.

| 환경 | Eureka | Swagger UI | DB |
| --- | --- | --- | --- |
| local | `http://localhost:18761` | `http://localhost:18081/swagger-ui.html` | `jdbc:postgresql://192.168.110.217:5432/bxcfdb` |
| dev | `http://192.168.110.217:18761` | `http://192.168.110.217:18081/swagger-ui.html` | `jdbc:postgresql://192.168.110.217:5432/bxcfdb` |

## DB 연결 정보

현재 `local`, `dev` profile은 동일한 PostgreSQL DB를 사용.

| 항목 | 값 |
| --- | --- |
| DBMS | PostgreSQL |
| Host | `192.168.110.217` |
| Port | `5432` |
| Database | `bxcfdb` |
| JDBC URL | `jdbc:postgresql://192.168.110.217:5432/bxcfdb` |
| Driver | `org.postgresql.Driver` |
| Username | `bxcf` |
| Password | `1111` |

직접 접속 예.

```bash
PGPASSWORD=1111 psql -h 192.168.110.217 -p 5432 -U bxcf -d bxcfdb
```

서비스별 datasource 설정 위치.

| 서비스 | local 설정 | dev 설정 | datasource |
| --- | --- | --- | --- |
| `auth-svc` | `src/main/resources/config/auth-svc/local/application-local.yml` | `src/main/resources/config/auth-svc/dev/application-dev.yml` | `jpa-main`, `mybatis-main` |
| `integration-svc` | `src/main/resources/config/integration-svc/local/application-local.yml` | `src/main/resources/config/integration-svc/dev/application-dev.yml` | 외부 API 연동 전용 |
| `mci-svc` | `src/main/resources/config/mci-svc/local/application-local.yml` | `src/main/resources/config/mci-svc/dev/application-dev.yml` | YAML 거래 registry, 외부 DB 미사용 |
| `product-svc` | `src/main/resources/config/product-svc/local/application-local.yml` | `src/main/resources/config/product-svc/dev/application-dev.yml` | `jpa-main`, `mybatis-main` |
| `system-svc` | `src/main/resources/config/system-svc/local/application-local.yml` | `src/main/resources/config/system-svc/dev/application-dev.yml` | `mybatis-main` |

운영/외부 배포 환경에서는 DB 계정과 비밀번호를 README나 profile 설정에 직접 두지 말고 환경변수, Vault, Kubernetes Secret 같은 외부 Secret 저장소로 분리 권장.

## Gateway 라우팅

Gateway 라우팅 설정 위치: `src/main/resources/config/api-gateway/application.yml`

| route id | 대상 서비스 | 조건 | 처리 |
| --- | --- | --- | --- |
| `auth-svc` | `lb://BWG-CHANNEL-BACKEND-AUTH-SVC` | `/channel/backend/api/v1/auth/**` | `StripPrefix=4` 후 auth-svc로 전달 |
| `integration-svc` | `lb://BWG-CHANNEL-BACKEND-INTEGRATION-SVC` | `/channel/backend/api/v1/integration/**` | `StripPrefix=4` 후 integration-svc로 전달 |
| `mci-svc` | `lb://BWG-CHANNEL-BACKEND-MCI-SVC` | `/channel/backend/api/v1/mci/**` | `StripPrefix=4` 후 mci-svc로 전달 |
| `product-svc` | `lb://BWG-CHANNEL-BACKEND-PRODUCT-SVC` | `/channel/backend/api/v1/product/**` | `StripPrefix=4` 후 product-svc로 전달 |
| `system-svc` | `lb://BWG-CHANNEL-BACKEND-SYSTEM-SVC` | `/channel/backend/api/v1/system/**` | `StripPrefix=4` 후 system-svc로 전달 |
| `*-api-docs` | 각 서비스 | `/{service}/v3/api-docs` | 각 서비스 context-path 기준 `/v3/api-docs`로 변환 |

`lb://` 라우팅은 Eureka에 등록된 인스턴스 목록 기준으로 Spring Cloud LoadBalancer가 대상 인스턴스를 선택. 같은 서비스가 여러 포트나 여러 서버에 떠 있어도 Eureka 등록과 네트워크 접근이 가능하면 Gateway에서 분산 호출 가능.

## 인증과 세션 흐름

현재 인증 흐름 핵심.

1. 클라이언트가 `auth-svc` 로그인 API 호출
2. `auth-svc`에서 사용자 정보 조회
3. `auth-svc`에서 `sessionId` 생성
4. access token에 `subject`, `roles`, `sessionId` claim 포함
5. refresh token은 DB 저장, 응답 본문 대신 HttpOnly 쿠키 전달
6. `auth-svc`에서 Redis/Valkey에 `session:{sessionId}` key로 `SessionContext` 저장
7. Gateway에서 이후 요청 access token 검증 후 내부 서비스로 인증 헤더 전달

```text
X-Auth-User
X-Auth-Roles
X-Auth-Session-Id
```

내부 서비스는 이 헤더로 JWT 재파싱 없이 사용자 ID, 권한, 세션 ID 확인 가능. 외부 사용자의 헤더 변조 방지는 product-svc/system-svc 같은 내부 서비스를 Gateway 뒤에 두고 직접 외부 접근을 차단하는 구성이 전제.

### Refresh Token Cookie

refresh token은 JavaScript에서 읽을 수 없도록 HttpOnly 쿠키로만 전달.

```js
await fetch("/channel/backend/api/v1/auth/refresh-token", {
  method: "POST",
  credentials: "include"
});
```

쿠키 설정 위치: `src/main/resources/config/auth-svc/{profile}/application-{profile}.yml`의 `app.auth.refresh-cookie`

| 옵션 | 현재 기본값 | 설명 |
| --- | --- | --- |
| `name` | `refreshToken` | refresh token 쿠키 이름 |
| `path` | `/channel/backend/api/v1/auth` | Gateway 기준 auth API 쿠키 path |
| `same-site` | `Lax` | SameSite 정책 |
| `secure` | `false` | local/dev 기준 false, HTTPS 운영환경에서는 true 권장 |
| `max-age-seconds` | `604800` | 7일 |

### Redis/Valkey 세션 컨텍스트

`session-context-common` 공통 세션 컨텍스트.

| 필드 | 설명 |
| --- | --- |
| `sessionId` | access token의 `sessionId` claim과 같은 값 |
| `userId` | JWT subject와 내부 사용자 식별 기준 |
| `roles` | 권한 판단에 사용할 권한 목록 |
| `authLevel` | MFA 등 추가 인증 수준 확장용 값 |
| `loginTime` | 세션 생성 시각 |
| `lastAccessTime` | 마지막 접근/갱신 시각 |

현재 Redis 연결 설정 위치: auth-svc profile 설정

```yaml
spring:
  data:
    redis:
      host: 192.168.110.217
      port: 6379
      timeout: 2s
```

Redis 서버는 개발 서버 podman으로 구축되어 있음

## 인증 제외 경로

Gateway `SecurityConfig`의 `PERMIT_URL_ARRAY` 기준. JWT 없이 접근 가능한 경로만 명시.

대표 경로.

```text
/channel/backend/api/v1/auth/login
/channel/backend/api/v1/auth/erp-login
/channel/backend/api/v1/auth/refresh-token
/channel/backend/api/v1/auth/signup
/channel/backend/api/v1/auth/password/find
/channel/backend/api/v1/auth/password/reset-request
/channel/backend/api/v1/auth/password/reset
/channel/backend/api/v1/integration/github/webhook
/swagger-ui/**
/v3/api-docs/**
/actuator/health
/actuator/info
```

### Integration

```text
POST /channel/backend/api/v1/integration/github/webhook
```

GitHub issue webhook을 수신해 Notion 할일 DB에 row를 생성한다. 이 endpoint는 사용자 JWT 대신 GitHub의 `X-Hub-Signature-256` HMAC-SHA256 서명을 검증한다.

필요 환경변수.

```text
GITHUB_WEBHOOK_SECRET
NOTION_ENABLED=true
NOTION_TOKEN
NOTION_DATABASE_ID
NOTION_VERSION=2022-06-28
NOTION_BASE_URL=https://api.notion.com
```

`/logout`은 인증된 access token 필요. Gateway가 검증한 내부 헤더의 `userId`, `sessionId` 기준으로 DB refresh token과 Redis 세션 삭제.

## API 목록

Gateway 기준 대표 API.

### Auth

```text
POST /channel/backend/api/v1/auth/login
POST /channel/backend/api/v1/auth/erp-login
POST /channel/backend/api/v1/auth/refresh-token
POST /channel/backend/api/v1/auth/logout
```

로그인 요청 예.

```json
{
  "data": {
    "usrId": "hong.gildong",
    "usrPwd": "password"
  }
}
```

로그인 응답의 `payload.accessToken`은 클라이언트가 이후 요청에 사용. refresh token은 HttpOnly 쿠키로 전달되고 JSON 응답에서는 제외.

```text
Authorization: Bearer <accessToken>
```

### Product

```text
POST /channel/backend/api/v1/product/list
POST /channel/backend/api/v1/product/detail/{productId}
```

### System - Menu

```text
POST /channel/backend/api/v1/system/menus/list
POST /channel/backend/api/v1/system/menus/{menuId}/detail
POST /channel/backend/api/v1/system/menus/create
POST /channel/backend/api/v1/system/menus/{menuId}/update
POST /channel/backend/api/v1/system/menus/{menuId}/delete
POST /channel/backend/api/v1/system/menus/{menuId}/actions/list
POST /channel/backend/api/v1/system/menus/roles/{roleId}/list
POST /channel/backend/api/v1/system/menus/roles/{roleId}/save
```

### System - Common Code

```text
POST /channel/backend/api/v1/system/common-codes/list
POST /channel/backend/api/v1/system/common-codes/groups/list
POST /channel/backend/api/v1/system/common-codes/{groupCd}/detail
POST /channel/backend/api/v1/system/common-codes/create
POST /channel/backend/api/v1/system/common-codes/{groupCd}/replace
POST /channel/backend/api/v1/system/common-codes/{groupCd}/delete
```

### System - Reference Data

```text
POST /channel/backend/api/v1/system/reference-data/versions/latest
```

### System - MCI Management

```text
POST /channel/backend/api/v1/system/mci/transactions/list
POST /channel/backend/api/v1/system/mci/transactions/{transactionCode}/detail
POST /channel/backend/api/v1/system/mci/transactions/create
POST /channel/backend/api/v1/system/mci/transactions/{transactionCode}/update
```

### MCI

```text
POST /channel/backend/api/v1/mci/execute
```

MCI 요청은 `MciRequest` 표준 형식을 사용한다. 현재 거래 설정은 `src/main/resources/config/mci-svc/application.yml`의 `mci.transactions`에 YAML로 관리한다.

## Swagger

Gateway에서 각 서비스 OpenAPI 문서를 모아서 제공.

```text
http://localhost:18081/swagger-ui.html
```

Swagger UI definition 목록.

| 이름 | api-docs |
| --- | --- |
| auth-svc | `/auth-svc/v3/api-docs` |
| product-svc | `/product-svc/v3/api-docs` |
| system-svc | `/system-svc/v3/api-docs` |
| integration-svc | `/integration-svc/v3/api-docs` |
| mci-svc | `/mci-svc/v3/api-docs` |

Swagger에서 인증 API 호출 순서.

1. `auth-svc` definition에서 로그인 API 호출
2. 응답의 `payload.accessToken` 복사
3. Swagger UI 상단 `Authorize`에 access token 입력
4. `product-svc` 또는 `system-svc` API 호출

### Swagger에서 로그아웃 테스트

Gateway Swagger(`http://localhost:18081/swagger-ui.html`)에서 테스트하는 것이 권장 흐름.

1. `auth-svc` definition에서 `login` API 호출
2. 응답의 `payload.accessToken` 복사
3. Swagger UI 상단 `Authorize`에 `Bearer <accessToken>` 형식으로 입력
4. `auth-svc` definition에서 `logout` API 호출

Gateway를 통해 호출하면 Gateway의 JWT 필터가 access token을 검증한 뒤 내부 요청에 아래 헤더를 자동 주입한다.

```text
X-Auth-User
X-Auth-Roles
X-Auth-Session-Id
```

따라서 Gateway Swagger에서는 `X-Auth-User`, `X-Auth-Session-Id`를 직접 찾거나 입력하지 않고 `Authorization`만 넣는 흐름이 정상이다.

auth-svc를 직접 호출해 테스트해야 한다면 내부 인증 헤더를 수동으로 넣어야 한다.

```text
X-Auth-User: <로그인 사용자 ID>
X-Auth-Session-Id: <access token의 sessionId claim>
```

`X-Auth-Session-Id`는 서버 로그에서 찾기보다 access token의 JWT payload에서 확인한다. JWT는 `header.payload.signature` 구조이므로 가운데 `payload` 부분만 Base64URL 디코딩하면 `sessionId` claim을 볼 수 있다. 실제 access token 전체를 외부 AI, 공개 웹 디코더, 채팅 도구에 붙여 넣지 않는다. 필요하면 로컬 도구나 브라우저 개발자 도구 콘솔에서 payload만 확인한다.

브라우저 콘솔 예시.

```js
const token = "<accessToken>";
JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
```

출력 JSON의 `sub`가 `X-Auth-User`, `sessionId`가 `X-Auth-Session-Id`에 해당한다.

## 공통 응답

기본 응답 구조: `ApiResponse<T>`

```json
{
  "success": true,
  "code": "0",
  "msg": "success",
  "payload": {}
}
```

### 예외 처리 원칙

- 의미를 아는 업무 실패는 도메인 ErrorCode와 BwgException 계열로 명시한다.
- 필요한 ErrorCode enum은 공통 개발팀에 요청하여 정의한다.
- BwgException에는 반드시 ErrorCode와 message를 담는다.
- 의미를 모르는 시스템/인프라/예상 밖 예외는 감싸지 않고 전파한다.
- MVC 요청의 예외는 GlobalRestExceptionAdvice가 처리하고, Security/Gateway 경계의 예외는 각 security handler 또는 gateway advice가 처리한다.
- 최종 ApiResponse 변환은 GlobalRestExceptionAdvice가 담당한다.

## 에러 코드 범위

| 영역 | 코드 범위 | 예 |
| --- | --- | --- |
| Common | `-2001`, `-2003`, `-2004`, `-4001` ~ `-4004`, `-9001`, `-9002`, `-9999` | validation, JSON 파싱, DB, timeout, server 공통 오류 |
| Auth | `-1001` ~ `-1005` | 토큰 오류, 인증 실패 |
| Business Common | `-3001` ~ `-3999` | 업무 검증 오류, 업무 데이터 없음 |
| Product | `-5001` ~ `-5999` | 상품 입력값 오류, 상품 없음, 상품 저장 오류 |
| MCI | `-6001` ~ `-6999` | 요청 검증, 거래 라우팅, Adapter/Mapper 구성 오류 |
| Gateway | `-9999` | Gateway 내부 오류 |

## OpenAPI TypeBridge

OpenAPI 관련 코드 위치.

| 영역 | 위치 |
| --- | --- |
| OpenAPI 공통 유틸 | `libs/common/src/main/java/com/bwg/channel/backend/common/openapi` |
| OpenAPI customizer | `libs/common/src/main/java/com/bwg/channel/backend/common/openapi/customizer` |
| TypeBridge | `libs/common/src/main/java/com/bwg/channel/backend/common/openapi/typebridge` |

주요 어노테이션.

| 어노테이션 | 역할 |
| --- | --- |
| `@ApiDto` | 요청/응답 DTO 방향과 endpoint 목록 정의 |
| `@ApiField` | 필드 설명, 예시, 필수/선택/제외 endpoint 정의 |
| `@ApiType` | `REQUEST`, `RESPONSE`, `LEGACY` 방향 구분 |

예.

```java
@ApiDto(type = ApiType.REQUEST, name = "Auth", endpoints = {"login", "erp-login"})
public class LoginReqDto {

    @ApiField(description = "사용자 ID", example = "hong.gildong", required = {"login", "erp-login"})
    private String usrId;

    @ApiField(description = "비밀번호", format = "password", required = {"login"})
    private String usrPwd;
}
```

## 설정 파일 구조

설정 파일은 루트의 `src/main/resources/config` 아래에 모음. Gradle `processResources`에서 선택된 profile 파일만 각 모듈 jar에 포함.

```text
src/main/resources/config/
├─ api-gateway/
│  ├─ application.yml
│  ├─ local/application-local.yml
│  └─ dev/application-dev.yml
├─ auth-svc/
├─ integration-svc/
├─ mci-svc/
├─ product-svc/
├─ system-svc/
├─ discovery-svc/
└─ security-common/
   ├─ local/application-local.yml
   └─ dev/application-dev.yml
```

profile 기본값: `local`

```bash
./gradlew :services:auth-svc:bootJar -Pprofile=dev
```

`security-common` 설정은 auth-svc와 api-gateway에서 `spring.config.import`로 참조.

## 빌드

전체 주요 서비스 bootJar.

```bash
./gradlew :infra:discovery-svc:bootJar :infra:api-gateway:bootJar :services:auth-svc:bootJar :services:integration-svc:bootJar :services:mci-svc:bootJar :services:product-svc:bootJar :services:system-svc:bootJar -Pprofile=local
```

빌드 결과.

```text
build/dist/bx-cf-be/*.jar
```

개별 빌드 예.

```bash
./gradlew :services:auth-svc:bootJar -Pprofile=local
./gradlew :services:integration-svc:bootJar -Pprofile=local
./gradlew :services:mci-svc:bootJar -Pprofile=local
./gradlew :infra:api-gateway:bootJar -Pprofile=local
```

## 실행

jar 직접 실행 예.

```bash
java -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul \
     -Dspring.profiles.active=local \
     -jar build/dist/bx-cf-be/auth-svc-0.0.1-SNAPSHOT.jar
```

Gradle bootRun 사용 예.

```bash
./gradlew :infra:discovery-svc:bootRun -Pprofile=local
./gradlew :services:auth-svc:bootRun -Pprofile=local
./gradlew :services:integration-svc:bootRun -Pprofile=local
./gradlew :services:mci-svc:bootRun -Pprofile=local
./gradlew :services:product-svc:bootRun -Pprofile=local
./gradlew :services:system-svc:bootRun -Pprofile=local
./gradlew :infra:api-gateway:bootRun -Pprofile=local
```

## 테스트

인증/세션/Gateway 관련 테스트.

```bash
./gradlew :services:auth-svc:test
./gradlew :services:integration-svc:test
./gradlew :services:mci-svc:test
./gradlew :libs:mci-common:test
./gradlew :libs:security-common:test
./gradlew :libs:session-context-common:test
./gradlew :infra:api-gateway:test
```

전체 테스트.

```bash
./gradlew test
```

## 개발 환경

| 항목 | 버전/기준 |
| --- | --- |
| Java | 21 |
| Gradle Wrapper | 8.14.3 |
| Spring Boot | 3.3.4 |
| Spring Cloud | 2023.0.1 |
| PostgreSQL | 16 계열 |
| Redis/Valkey | 외부 서버 필요 |

문자 인코딩.

- `.editorconfig`의 `charset = utf-8`
- `gradle.properties`의 `-Dfile.encoding=UTF-8`
- Java compile encoding `UTF-8`

## 배포 참고

`.github/workflows/deploy-dev.yml`은 `develop` push 시 변경 경로 기준으로 필요한 서비스만 빌드/배포.

| 변경 경로 | 배포 대상 |
| --- | --- |
| `services/auth-svc/**` | auth-svc |
| `services/integration-svc/**`, `src/main/resources/config/integration-svc/**` | integration-svc |
| `services/mci-svc/**`, `libs/mci-common/**`, `src/main/resources/config/mci-svc/**` | mci-svc |
| `services/product-svc/**` | product-svc |
| `services/system-svc/**`, `src/main/resources/config/system-svc/**` | system-svc |
| `libs/**`, `infra/**`, `scripts/**`, `.github/workflows/**` | 전체 서비스 |

## GitHub Project

- Issues: `https://github.com/yooyongbeom/bx-cf-be/issues`
- Project board: `https://github.com/users/yooyongbeom/projects/2`

## 참고 자료

`reffile/` 아래 프로젝트 참고 문서와 Postman collection 위치..
