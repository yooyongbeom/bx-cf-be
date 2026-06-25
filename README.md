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
| local | http://localhost:18761 | jdbc:postgresql://192.168.110.217:5432/bxcfdb |
| dev | http://192.168.110.217:18761 | jdbc:postgresql://192.168.110.217:5432/bxcfdb |

Postman: `reffile/BX-CF-BE-LCL.postman_collection.json`

## Swagger (API 문서)

게이트웨이에서 서비스별 문서를 한 화면에 모아 제공한다. (게이트웨이 기동 후 접속)

| 환경 | Swagger UI |
| --- | --- |
| local | http://localhost:18081/swagger-ui.html |
| dev | http://192.168.110.217:18081/swagger-ui.html |

- 우측 상단 **Select a definition** 드롭다운에서 auth-svc / product-svc 전환.
- Schemas 목록에는 DTO만 표시(공통 응답 래퍼 제외). 응답 모델에는 공통 응답 구조(success/code/msg/payload)가 인라인으로 표시된다.

### Authorize (JWT) 사용법

product-svc API는 JWT가 필요하다(auth-svc 로그인/토큰 발급은 불필요). 토큰을 발급받아 Authorize에 등록하면 이후 요청에 자동으로 `Authorization: Bearer ...` 가 붙는다.

1. **Select a definition → auth-svc** 선택.
2. `POST /channel/backend/api/v1/auth/login` → **Try it out** → 입력 후 **Execute**.

   ```json
   // 응답 payload 에서 accessToken 복사
   {
     "success": true, "code": "0", "msg": "success",
     "payload": { "accessToken": "eyJhbGciOi...", "refreshToken": "..." }
   }
   ```

3. 우측 상단 **Authorize 🔒** 클릭 → `bearerAuth` 입력란에 **accessToken 값만** 붙여넣기(앞에 `Bearer ` 안 붙여도 됨) → **Authorize**.
4. **Select a definition → product-svc** 로 바꿔 API 호출 → 자물쇠가 잠긴 상태로 토큰이 자동 전송된다.

> 한 번 Authorize한 토큰은 새로고침/재접속에도 유지된다(`springdoc.swagger-ui.persist-authorization`). 단 토큰 만료 시간이 지나면 `-1004`(401)가 나므로 재로그인 후 다시 Authorize 한다.
> local 기본 만료는 테스트용으로 짧게 설정돼 있으니 필요 시 `config/auth-core/local/application-local.yml` 의 `access-token-validity-ms` 로 조정한다.

## 개발 환경 요구사항

| 항목 | 버전 |
| --- | --- |
| Java | 21 |
| Gradle | 8.8 |
| PostgreSQL | 16 |
| Spring Boot | 3.3.4 |

## type-bridge

`libs/type-bridge` 모듈이 SpringDoc OpenAPI Customizer를 통해 DTO → TypeScript 타입을 자동 생성한다.

### DTO 어노테이션

```java
@ApiDto(name = "Auth", endpoints = {"login", "erp-login"})
public class LoginDto {

    @ApiField(description = "사용자 ID", example = "hong.gildong", required = {"login", "erp-login"})
    private String usrId;

    @ApiField(description = "비밀번호", format = "password", required = {"login"}, exclude = {"erp-login"})
    private String usrPwd;

    @ApiField(description = "사용자명", responseOnly = true)
    private String usrNm;
}
```

- `@ApiDto(name, endpoints)` — OpenAPI 스키마 이름과 엔드포인트 목록 지정
- `@ApiField(required, exclude, responseOnly)` — 엔드포인트별 필드 포함/제외 제어
- `generateResponse = false` — 응답 스키마 생성 불필요한 경우 (ex. RefreshTknReqDto)

### 프론트엔드 TypeScript 타입 생성

```bash
npx openapi-typescript http://192.168.110.217:18081/v3/api-docs -o src/types/api.d.ts
```

생성 결과: `AuthLoginRequest`, `AuthErpLoginRequest`, `AuthResponse`, `ApiResponse«AuthResponse»` 등

## WBS / GitHub Projects

- **이슈 목록**: https://github.com/yooyongbeom/bx-cf-be/issues
- **Projects 보드**: https://github.com/users/yooyongbeom/projects/2

3단계 / 3개월 일정 (2026-07-01 ~ 2026-09-28):

| 단계 | 기간 | 주제 |
| --- | --- | --- |
| 1단계 | 7/1 ~ 7/27 | 기초 구조 완성 (인프라, 보안, API, 비즈니스 로직) |
| 2단계 | 7/28 ~ 8/30 | MSA 안정화 및 운영 기반 (CI/CD, 테스트, 모니터링) |
| 3단계 | 9/1 ~ 9/28 | 고급 MSA 패턴 및 배포 고도화 (Kafka, SAGA, Rolling Update) |

## scripts/

| 파일 | 설명 |
| --- | --- |
| `github-projects-rebuild.ps1` | 기존 이슈 전체 삭제 후 WBS 재구성 (이슈 생성 → 서브이슈 연결 → 로드맵 날짜 설정) |

실행 전 `gh auth login` 및 `gh auth refresh -s read:project` 필요.

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

## 설정 (프로파일)

설정 파일은 각 모듈이 아니라 루트의 `src/main/resources/config/` 아래에 모여 있고, 빌드 시 `processResources`가 **선택된 프로파일 파일만** 각 jar로 복사한다.

```
src/main/resources/config/
├── {module}/application.yml              모듈 공통 (항상 포함)
├── {module}/{profile}/application-{profile}.yml   프로파일별 (선택 포함)
└── auth-core/{profile}/...               auth-core 공통 설정 (application-authcore-{profile}.yml 로 포함)
```

- 프로파일은 빌드 인자 `-Pprofile=dev` 로 결정 (미지정 시 `local`).
- 예: `-Pprofile=dev` 로 빌드하면 `config/{module}/dev/` 의 파일만 패키징된다.
- 모듈 자체(`각 모듈/src/main/resources`)에 같은 이름의 파일을 두면 `duplicatesStrategy=EXCLUDE` 로 그쪽이 우선되어 위 공통 설정이 무시되니 주의.

```bash
# 빌드 시 프로파일 지정
./gradlew :services:auth-svc:bootJar -Pprofile=dev
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

상품 API는 게이트웨이에서 JWT 인증이 필요하다. 요청 헤더에 로그인으로 발급받은 토큰을 넣는다.

```
Authorization: Bearer <accessToken>
```

토큰이 없거나 유효하지 않으면 `-1003`, 만료 시 `-1004` 반환. (auth-svc 로그인/토큰 발급은 인증 불필요)

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
