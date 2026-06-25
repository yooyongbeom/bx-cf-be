# ================================================================
# BX-CF-BE GitHub Issues 전면 재구성
# - 기존 이슈 전체 삭제
# - 전체 WBS (완료 포함) 이슈 재생성
# - GraphQL addSubIssue 로 Epic > Week > Task 계층 연결
# - 완료 항목은 이슈 생성 후 close
#
# 사전 준비:
#   gh auth login
#   gh auth refresh -s read:project
#
# 실행:
#   .\scripts\github-projects-rebuild.ps1
# ================================================================

$Owner    = "yooyongbeom"
$Repo     = "$Owner/bx-cf-be"
$ProjId   = "PVT_kwHOAiImm84Bbakk"

# ── helpers ────────────────────────────────────────────────────────

function Get-NodeId($issueNum) {
    $q = 'query($o:String!,$r:String!,$n:Int!){repository(owner:$o,name:$r){issue(number:$n){id}}}'
    $res = gh api graphql --field "query=$q" --field "o=$Owner" --field "r=bx-cf-be" --field "n=$issueNum" | ConvertFrom-Json
    return $res.data.repository.issue.id
}

function Add-Sub($parentNum, $childNum) {
    $parentId = Get-NodeId $parentNum
    $childId  = Get-NodeId $childNum
    $m = 'mutation($p:ID!,$c:ID){addSubIssue(input:{issueId:$p,subIssueId:$c}){issue{number}subIssue{number}}}'
    gh api graphql --field "query=$m" --field "p=$parentId" --field "c=$childId" | Out-Null
}

function New-Issue($title, $labels, $milestone, $body = " ") {
    if (-not $body) { $body = " " }
    $out = gh issue create --repo $Repo --title $title --label $labels --milestone $milestone --body $body 2>&1
    $url = $out | Where-Object { $_ -match "^https://" } | Select-Object -First 1
    if (-not $url) { Write-Host "  !! FAILED: $title`n     $out" -ForegroundColor Red; return 0 }
    $num = [int]($url.Trim() -split '/')[-1]
    Write-Host "  #$num $title" -ForegroundColor Cyan
    return $num
}

function New-Epic($title, $body = " ") {
    if (-not $body) { $body = " " }
    $out = gh issue create --repo $Repo --title $title --label "epic" --body $body 2>&1
    $url = $out | Where-Object { $_ -match "^https://" } | Select-Object -First 1
    if (-not $url) { Write-Host "  !! FAILED: $title`n     $out" -ForegroundColor Red; return 0 }
    $num = [int]($url.Trim() -split '/')[-1]
    Write-Host "  #$num $title" -ForegroundColor Yellow
    return $num
}

function Close-Issue($num) {
    gh issue close $num --repo $Repo | Out-Null
}

# ── 1. 기존 이슈 전체 삭제 ────────────────────────────────────────

Write-Host "[1/5] Deleting existing issues..." -ForegroundColor Red
$existing = gh issue list --repo $Repo --limit 200 --state all --json number | ConvertFrom-Json
foreach ($i in $existing) {
    gh issue delete $i.number --repo $Repo --yes 2>$null
}
Write-Host "  Done."

# ── 2. Milestones ─────────────────────────────────────────────────

Write-Host "[2/5] Creating milestones..." -ForegroundColor Yellow
$existing_ms = gh api repos/$Repo/milestones --paginate 2>$null | ConvertFrom-Json | ForEach-Object { $_.title }
if ($existing_ms -notcontains "1단계") { gh api repos/$Repo/milestones -f title="1단계" -f due_on="2026-07-27T00:00:00Z" -f description="Phase 1: 7/1 - 7/27" | Out-Null }
if ($existing_ms -notcontains "2단계") { gh api repos/$Repo/milestones -f title="2단계" -f due_on="2026-08-30T00:00:00Z" -f description="Phase 2: 7/28 - 8/30" | Out-Null }
if ($existing_ms -notcontains "3단계") { gh api repos/$Repo/milestones -f title="3단계" -f due_on="2026-09-28T00:00:00Z" -f description="Phase 3: 9/1 - 9/28" | Out-Null }
Write-Host "  Done."

# ── 3. Labels ──────────────────────────────────────────────────────

Write-Host "[3/5] Creating labels..." -ForegroundColor Yellow
$labels = @(
    @("epic","0052cc"),@("infra","0075ca"),@("security","e4e669"),
    @("api","d93f0b"),@("business","5319e7"),@("test","f9d0c4"),
    @("monitoring","bfdadc"),@("build","c2e0c6"),@("done","4ade80"),
    @("in-progress","60a5fa"),@("P1","b60205"),@("P2","fbca04"),@("P3","0e8a16")
)
foreach ($l in $labels) {
    gh api repos/$Repo/labels -f name=$($l[0]) -f color=$($l[1]) 2>$null | Out-Null
}

# ── 4. Issues 생성 ─────────────────────────────────────────────────

Write-Host "[4/5] Creating issues..." -ForegroundColor Yellow

# ============================================================
# 1단계 Epic
# ============================================================
Write-Host "  [1단계] Epic" -ForegroundColor Magenta
$e1 = New-Epic "[1단계] 핵심 기능 완성" "## 목표`n7/1 - 7/27 동안 프로젝트 기반 구조, 인프라, 보안, API, 비즈니스 로직의 핵심 기능을 완성한다.`n`n## 주차별 계획`n- **W1** 프로젝트 기반 및 인프라 완성`n- **W2** 보안 (Spring Security / JWT / 권한) 완성`n- **W3** API 표준화 및 문서화 완성`n- **W4** 비즈니스 로직 구현 완성"

# W1
Write-Host "  W1" -ForegroundColor DarkCyan
$w1 = New-Issue "[W1] 프로젝트 기반 & 인프라 완성 (7/1 ~ 7/6)" "infra,P1" "1단계" "## 목표`nGradle 멀티모듈 monorepo 구조를 완성하고 MSA 인프라(Eureka, Gateway) 기반을 구축한다.`n`n## 완료 기준`n- [ ] 멀티모듈 빌드가 정상 동작한다`n- [ ] Eureka 대시보드에서 모든 서비스가 등록 확인된다`n- [ ] Gateway를 통해 auth-svc, product-svc 라우팅이 동작한다`n- [ ] Gateway 인증 필터가 JWT를 검증한다`n- [ ] Graceful Shutdown 시 진행 중 요청이 완료 후 종료된다"

$t = @()
$t += New-Issue "[W1] monorepo 구성 (libs/infra/services)" "infra,done,P1" "1단계" "## 개요`nGradle 멀티모듈 구조로 `libs/`, `infra/`, `services/` 디렉터리를 분리하여 관심사별 모듈을 구성한다.`n`n## 주요 작업`n- `settings.gradle`에 모든 서브모듈 등록`n- 루트 `build.gradle`에 공통 의존성 및 플러그인 설정`n- 모듈 간 의존성 참조(`api`, `implementation`) 설정`n`n## 완료 기준`n- [ ] `./gradlew build`가 전체 모듈에서 성공한다"

$t += New-Issue "[W1] libs/common 모듈" "infra,done,P1" "1단계" "## 개요`n전체 서비스에서 공통으로 사용하는 예외 처리, AOP, JPA/MyBatis 기반 설정을 제공하는 라이브러리 모듈이다.`n`n## 주요 작업`n- `ApiResponse<T>` 공통 응답 래퍼 정의`n- `BwgException` / `BwgErrorCode` 기반 예외 체계 구성`n- `JpaMainConfigBase`, `MyBatisMainConfigBase` 이중 데이터소스 기반 클래스 제공`n- `GlobalExceptionHandler` AOP 기반 공통 예외 처리`n`n## 완료 기준`n- [ ] 각 서비스가 common을 상속하여 별도 설정 없이 동작한다"

$t += New-Issue "[W1] libs/auth-core 모듈" "security,done,P1" "1단계" "## 개요`nJWT 인증 필터와 인증 관련 에러 코드를 제공하는 라이브러리 모듈이다. Gateway와 각 서비스가 공통으로 참조한다.`n`n## 주요 작업`n- `JwtAuthFilter` (OncePerRequestFilter) 구현`n- `AuthErrorCode` 열거형 정의 (-1001 ~ -1005)`n- `BwgAuthException` 커스텀 예외 구현`n- 토큰 파싱 / 검증 유틸 제공`n`n## 완료 기준`n- [ ] 유효하지 않은 토큰 요청 시 `-1002` 응답이 반환된다`n- [ ] 만료된 토큰 요청 시 `-1004` 응답이 반환된다"

$t += New-Issue "[W1] libs/type-bridge 모듈" "api,done,P1" "1단계" "## 개요`nSpringDoc OpenAPI Customizer를 통해 DTO 어노테이션(`@ApiDto`, `@ApiField`)을 OpenAPI 스키마로 변환하여 프론트엔드 TypeScript 타입 자동 생성을 지원한다.`n`n## 주요 작업`n- `@ApiDto(name, endpoints)` 어노테이션 정의`n- `@ApiField(description, example, required, exclude, responseOnly, format, ...)` 어노테이션 정의`n- `TypeBridgeOpenApiCustomizer` 구현 (스키마 등록)`n- `TypeBridgeOperationCustomizer` 구현 (requestBody / response 교체)`n`n## 완료 기준`n- [ ] `/v3/api-docs`에 `AuthLoginRequest`, `AuthResponse` 등 엔드포인트별 스키마가 등록된다`n- [ ] `npx openapi-typescript` 실행 시 TypeScript 타입이 정상 생성된다"

$t += New-Issue "[W1] Eureka Server/Client 등록" "infra,done,P1" "1단계" "## 개요`nSpring Cloud Netflix Eureka를 사용하여 서비스 디스커버리를 구성한다. 각 서비스는 Eureka에 자신을 등록하고 다른 서비스를 동적으로 탐색한다.`n`n## 주요 작업`n- `infra/discovery-svc` Eureka Server 구성 (포트 18761)`n- `auth-svc`, `product-svc`, `api-gateway`에 Eureka Client 설정`n- 환경별 Eureka 서버 URL 설정 (local/dev)`n`n## 완료 기준`n- [ ] `http://localhost:18761`에서 모든 서비스 등록 확인`n- [ ] 서비스 재기동 후 자동 재등록된다"

$t += New-Issue "[W1] API Gateway 라우팅 / LoadBalancer" "infra,done,P1" "1단계" "## 개요`nSpring Cloud Gateway를 사용하여 클라이언트 요청을 각 마이크로서비스로 라우팅하고 로드밸런싱을 적용한다.`n`n## 주요 작업`n- `api-gateway` 모듈 구성 (포트 18081)`n- 서비스별 라우트 설정 (`/channel/backend/api/v1/auth/**` → auth-svc)`n- Spring Cloud LoadBalancer로 Eureka 기반 서비스 탐색 라우팅`n- Swagger 통합 (각 서비스 API 문서를 Gateway에서 통합 제공)`n`n## 완료 기준`n- [ ] Gateway를 통해 auth-svc, product-svc API 호출이 정상 동작한다"

$t += New-Issue "[W1] 환경별 설정 관리 (local/dev/prod)" "infra,done,P1" "1단계" "## 개요`n루트 `src/main/resources/config/` 하위에 환경별 설정 파일을 중앙 관리하고, 빌드 시 `-Pprofile` 인자로 환경을 선택한다.`n`n## 주요 작업`n- `config/{module}/application.yml` (공통)`n- `config/{module}/{profile}/application-{profile}.yml` (환경별)`n- `processResources`에서 선택된 프로파일 파일만 패키징`n- local 기본값, dev/prod 분리`n`n## 완료 기준`n- [ ] `-Pprofile=dev` 빌드 후 dev 설정만 포함된 jar가 생성된다"

$t += New-Issue "[W1] Gateway 인증 필터 (JWT 검증)" "infra,security,P1" "1단계" "## 개요`nGateway에서 인증이 필요한 라우트에 대해 JWT를 검증하는 글로벌 필터를 구현한다. 인증 불필요 경로(로그인, 토큰 재발급)는 필터를 우회한다.`n`n## 주요 작업`n- `GlobalAuthFilter` (GatewayFilter) 구현`n- 화이트리스트 경로 설정 (`/auth/login`, `/auth/refresh-token`)`n- JWT 검증 실패 시 `-1002` / `-1003` / `-1004` 에러 응답`n- 검증 성공 시 `X-Auth-User` 헤더에 사용자 정보 전달`n`n## 완료 기준`n- [ ] 토큰 없이 product-svc 호출 시 `-1003` 반환`n- [ ] 만료 토큰으로 호출 시 `-1004` 반환`n- [ ] 유효 토큰으로 호출 시 정상 응답"

$t += New-Issue "[W1] Gateway CORS 설정" "infra,P1" "1단계" "## 개요`nGateway 레벨에서 CORS 정책을 일괄 적용한다. 프론트엔드(Vue/React)에서 다른 Origin으로 API를 호출할 때 브라우저 CORS 오류가 발생하지 않도록 한다.`n`n## 주요 작업`n- `WebFluxConfigurer` 또는 Gateway `GlobalCorsConfig` 구현`n- 허용 Origin, Method, Header 설정 (환경별 분리)`n- `OPTIONS` preflight 요청 처리`n`n## 완료 기준`n- [ ] 로컬 프론트엔드(`http://localhost:3000`)에서 API 호출 시 CORS 오류 없음"

$t += New-Issue "[W1] Graceful Shutdown" "infra,P1" "1단계" "## 개요`n서버 종료 시그널(`SIGTERM`) 수신 후 진행 중인 요청을 완료하고 안전하게 종료한다. Kubernetes Rolling Update 시 요청 유실을 방지한다.`n`n## 주요 작업`n- `server.shutdown=graceful` 설정`n- `spring.lifecycle.timeout-per-shutdown-phase` 설정 (기본 30s)`n- 종료 시 Eureka 디등록 선행 처리`n- 헬스체크 엔드포인트가 종료 전 `OUT_OF_SERVICE`를 반환하도록 설정`n`n## 완료 기준`n- [ ] 종료 시그널 후 진행 중 요청이 정상 완료된다`n- [ ] 새 요청은 다른 인스턴스로 라우팅된다"

for ($i=0; $i -lt $t.Count; $i++) {
    if ($i -le 6) { Close-Issue $t[$i] }
}

# W2
Write-Host "  W2" -ForegroundColor DarkCyan
$w2 = New-Issue "[W2] 보안 완성 (7/7 ~ 7/13)" "security,P1" "1단계" "## 목표`nSpring Security 기반 인증/인가 체계를 완성하고 에러 코드 표준화, 권한 관리 API를 구현한다.`n`n## 완료 기준`n- [ ] 로그인 / 토큰 재발급 API가 정상 동작한다`n- [ ] 에러 코드가 전사 표준으로 통일된다`n- [ ] 권한 기반 API 접근 제어가 동작한다"

$t2 = @()
$t2 += New-Issue "[W2] Spring Security 구성" "security,done,P1" "1단계" "## 개요`nSpring Security를 설정하여 인증/인가 흐름을 구성한다. JWT 기반 Stateless 방식으로 세션을 사용하지 않는다.`n`n## 주요 작업`n- `SecurityConfig` 구성 (`HttpSecurity`, `SessionCreationPolicy.STATELESS`)`n- `JwtAuthFilter`를 Security 필터 체인에 등록`n- 인증 불필요 경로 허용 설정`n- CSRF 비활성화`n`n## 완료 기준`n- [ ] 인증 없이 보호된 API 접근 시 401 반환`n- [ ] 인증 후 보호된 API 접근 시 정상 응답"

$t2 += New-Issue "[W2] JWT 발급 / 검증 로직" "security,done,P1" "1단계" "## 개요`nHS256 알고리즘으로 JWT를 발급하고 검증한다. `auth-core` 모듈의 유틸을 통해 토큰 파싱 및 클레임 추출을 처리한다.`n`n## 주요 작업`n- `JwtTokenProvider` 구현 (발급, 검증, 클레임 추출)`n- AccessToken / RefreshToken 분리 발급`n- 시크릿 키 환경별 분리 관리`n- 토큰 만료 시간 설정 (AccessToken: 30분, RefreshToken: 7일)`n`n## 완료 기준`n- [ ] 로그인 시 AccessToken, RefreshToken이 응답에 포함된다`n- [ ] 만료된 토큰 검증 시 `ExpiredJwtException`이 발생한다"

$t2 += New-Issue "[W2] Refresh Token 관리" "security,done,P1" "1단계" "## 개요`nRefreshToken을 DB에 저장하고 재발급 요청 시 유효성을 검증하여 새 AccessToken을 발급한다.`n`n## 주요 작업`n- RefreshToken 엔티티 및 Repository 구현`n- `/auth/refresh-token` API 구현`n- RefreshToken 탈취 방지 (1회 사용 후 교체, Rotation)`n- 로그아웃 시 RefreshToken 무효화`n`n## 완료 기준`n- [ ] 만료된 AccessToken을 RefreshToken으로 재발급할 수 있다`n- [ ] 이미 사용된 RefreshToken 재사용 시 오류가 반환된다"

$t2 += New-Issue "[W2] 인증 API (/login, /refresh-token)" "api,done,P1" "1단계" "## 개요`n사용자 로그인과 토큰 재발급 REST API를 구현한다.`n`n## API 명세`n`n    POST /channel/backend/api/v1/auth/login`n      Request:  { usrId, usrPwd(SHA-256) }`n      Response: { accessToken, refreshToken, usrNm, roles, ... }`n`n    POST /channel/backend/api/v1/auth/refresh-token`n      Request:  { refreshToken }`n      Response: { accessToken, refreshToken }"

$t2 += New-Issue "[W2] 에러 코드 체계 표준화" "api,P1" "1단계" "## 개요`n전사 에러 코드를 모듈별로 분리하여 표준화한다. 에러 코드 충돌 없이 서비스별 독립적인 에러 관리가 가능하도록 한다.`n`n## 에러 코드 대역 정의`n| 대역 | 모듈 |`n|------|------|`n| -1000 ~ -1999 | libs/auth-core (인증/인가) |`n| -5000 ~ -5999 | product-svc |`n| -9000 ~ -9999 | Gateway / 공통 |`n`n## 주요 작업`n- `BwgErrorCode` 인터페이스 정의`n- 각 모듈별 `XxxErrorCode` enum 구현`n- `GlobalExceptionHandler`에서 에러 코드별 응답 포맷 통일`n`n## 완료 기준`n- [ ] 모든 에러 응답이 `{ success: false, code: -XXXX, msg: ... }` 형식으로 반환된다"

$t2 += New-Issue "[W2] 권한 도메인 설계" "security,P1" "1단계" "## 개요`n역할(Role) 기반 접근 제어(RBAC)를 위한 도메인 모델을 설계한다.`n`n## 주요 작업`n- `Role` 엔티티 설계 (ROLE_ADMIN, ROLE_USER, ROLE_MANAGER 등)`n- `UserRole` 매핑 테이블 설계`n- Spring Security `GrantedAuthority`와 연동`n- JWT 클레임에 roles 포함`n`n## 완료 기준`n- [ ] 로그인 응답에 `roles` 목록이 포함된다`n- [ ] DB에 사용자-역할 매핑이 저장된다"

$t2 += New-Issue "[W2] 권한 API 구현" "security,P1" "1단계" "## 개요`n권한 조회/변경 API를 구현한다. 관리자는 사용자에게 역할을 부여하거나 회수할 수 있다.`n`n## API 명세`n`n    GET    /channel/backend/api/v1/auth/roles         권한 목록 조회`n    POST   /channel/backend/api/v1/auth/roles/{userId}  권한 부여`n    DELETE /channel/backend/api/v1/auth/roles/{userId}  권한 회수"

$t2 += New-Issue "[W2] CORS 설정 (서비스 레벨)" "security,P1" "1단계" "## 개요`n서비스 레벨에서 CORS를 설정한다. Gateway를 거치지 않는 직접 호출(로컬 개발 환경 등)에서도 CORS 오류가 없도록 한다.`n`n## 주요 작업`n- `WebMvcConfigurer`에서 `addCorsMappings` 구현`n- 환경별 허용 Origin 분리 설정 (local: `*`, dev/prod: 지정 도메인)`n`n## 완료 기준`n- [ ] 서비스를 직접 호출 시 CORS 오류가 발생하지 않는다"

for ($i=0; $i -lt 4; $i++) { Close-Issue $t2[$i] }

# W3
Write-Host "  W3" -ForegroundColor DarkCyan
$w3 = New-Issue "[W3] API 완성 (7/14 ~ 7/20)" "api,P1" "1단계" "## 목표`nREST API 표준화, 문서화, 예외 처리, 유효성 검사를 완성하여 프론트엔드 연동 준비를 마친다.`n`n## 완료 기준`n- [ ] Swagger UI에서 모든 API 확인 가능`n- [ ] 잘못된 요청에 대해 표준 에러 응답이 반환된다`n- [ ] TypeScript 타입이 자동 생성된다"

$t3 = @()
$t3 += New-Issue "[W3] REST API 응답 표준화 (ApiResponse)" "api,done,P1" "1단계" "## 개요`n모든 API 응답을 ApiResponse<T> 래퍼로 통일한다.`n`n## 응답 포맷`n`n    { success: true, code: '0', msg: 'success', payload: { ... } }"

$t3 += New-Issue "[W3] @ApiDto / @ApiField 어노테이션 체계" "api,done,P1" "1단계" "## 개요`n@Schema 대신 커스텀 어노테이션으로 DTO를 선언하면 type-bridge가 OpenAPI 스키마를 자동 생성한다.`n`n## 어노테이션 속성`n- @ApiDto(name, endpoints, generateResponse)`n- @ApiField(description, example, required, exclude, responseOnly, format, nullable, minLength, maxLength, allowableValues)"

$t3 += New-Issue "[W3] Swagger / OpenAPI 문서화" "api,done,P1" "1단계" "## 개요`nspringdoc-openapi를 사용하여 Swagger UI를 제공한다. Gateway에서 여러 서비스의 API를 하나의 UI로 통합 제공한다.`n`n## 접속 정보`n- local: http://localhost:18081/swagger-ui.html`n- dev: http://192.168.110.217:18081/swagger-ui.html`n`n## 완료 기준`n- [ ] Swagger UI에서 auth-svc / product-svc 전환이 가능하다`n- [ ] JWT Authorize 후 인증이 필요한 API 호출이 가능하다"

$t3 += New-Issue "[W3] openapi-typescript 프론트 연동" "api,done,P1" "1단계" "## 개요`nOpenAPI 스펙을 기반으로 프론트엔드 TypeScript 타입을 자동 생성하여 API 타입 안전성을 확보한다.`n`n## 사용법`n`n    npx openapi-typescript http://192.168.110.217:18081/v3/api-docs -o src/types/api.d.ts`n`n## 생성되는 타입 예시`n- AuthLoginRequest, AuthErpLoginRequest`n- AuthResponse, ApiResponse_AuthResponse_"

$t3 += New-Issue "[W3] 글로벌 예외 처리" "api,P1" "1단계" "## 개요`n`@RestControllerAdvice`를 사용하여 애플리케이션 전체 예외를 일관된 응답 포맷으로 처리한다.`n`n## 처리 대상`n- `BwgException` → 비즈니스 에러 코드 응답`n- `MethodArgumentNotValidException` → 유효성 검사 실패 (-1001)`n- `HttpMessageNotReadableException` → JSON 파싱 오류 (-2003)`n- `Exception` → 서버 내부 오류 (-9999)`n`n## 완료 기준`n- [ ] 모든 예외가 `ApiResponse` 형식으로 반환된다`n- [ ] 스택 트레이스가 응답에 노출되지 않는다"

$t3 += New-Issue "[W3] Validation 적용 (@Valid)" "api,P1" "1단계" "## 개요`nBean Validation(`@Valid`)을 사용하여 요청 DTO의 유효성을 검사한다. 유효성 실패 시 표준 에러 응답을 반환한다.`n`n## 주요 작업`n- DTO에 `@NotBlank`, `@NotNull`, `@Size`, `@Pattern` 등 어노테이션 적용`n- `@Valid` / `@Validated`를 Controller 파라미터에 적용`n- `MethodArgumentNotValidException` 핸들러에서 필드별 에러 메시지 반환`n`n## 완료 기준`n- [ ] 필수 필드 누락 시 `-1001`과 어떤 필드가 누락인지 응답에 포함된다"

$t3 += New-Issue "[W3] API Versioning (/v1/) 적용" "api,P1" "1단계" "## 개요`n모든 API 경로에 버전 prefix(`/v1/`)를 적용하여 향후 하위 호환성을 유지할 수 있도록 한다.`n`n## 주요 작업`n- `@RequestMapping(/channel/backend/api/v1/...)` 전체 적용 확인`n- Gateway 라우트 경로 일치 여부 검증`n- API 문서에 버전 명시`n`n## 완료 기준`n- [ ] 모든 API가 `/channel/backend/api/v1/` 경로로 동작한다"

$t3 += New-Issue "[W3] Idempotency (멱등성 처리)" "api,P1" "1단계" "## 개요`n동일 요청을 중복 전송해도 동일한 결과가 반환되도록 멱등성을 보장한다. 네트워크 재시도로 인한 중복 처리를 방지한다.`n`n## 주요 작업`n- `Idempotency-Key` 헤더 기반 중복 요청 감지`n- Redis에 요청 키와 응답 캐싱 (TTL: 24시간)`n- 동일 키 재요청 시 캐싱된 응답 반환`n`n## 완료 기준`n- [ ] 동일 `Idempotency-Key`로 2회 요청 시 두 번째 요청에서 캐싱된 응답이 반환된다"

for ($i=0; $i -lt 4; $i++) { Close-Issue $t3[$i] }

# W4
Write-Host "  W4" -ForegroundColor DarkCyan
$w4 = New-Issue "[W4] 비즈니스 로직 완성 (7/21 ~ 7/27)" "business,P1" "1단계" "## 목표`nJPA Entity 설계, DB 마이그레이션, 트랜잭션 관리를 완성하고 auth-svc / product-svc 핵심 비즈니스 로직을 구현한다.`n`n## 완료 기준`n- [ ] JPA Entity가 DB 테이블과 매핑되어 CRUD가 동작한다`n- [ ] Flyway로 DB 스키마 버전 관리가 된다`n- [ ] auth-svc, product-svc 핵심 기능이 동작한다"

$t4 = @()
$t4 += New-Issue "[W4] JPA + MyBatis 이중 데이터소스" "business,done,P1" "1단계" "## 개요`n단순 CRUD는 JPA로, 복잡한 조회는 MyBatis로 처리하는 이중 데이터소스 구조를 구성한다.`n`n## 주요 작업`n- `JpaMainConfigBase` / `MyBatisMainConfigBase` 기반 클래스 제공`n- 서비스별 Config가 Base를 상속하여 데이터소스 구성`n- `@Transactional` 동작 범위 명확화`n`n## 완료 기준`n- [ ] JPA와 MyBatis가 동일 DataSource를 공유하며 동작한다"

$t4 += New-Issue "[W4] 계층형 아키텍처 정립 (Controller/Svc/Repo)" "business,P1" "1단계" "## 개요`n각 서비스가 Controller → Service → Repository 계층 구조를 일관되게 따르도록 가이드라인을 수립한다.`n`n## 주요 작업`n- 레이어별 책임 정의 문서화`n- DTO 변환 위치 결정 (Service 레이어에서 변환)`n- `@Service`, `@Repository` 어노테이션 일관 적용`n- 패키지 구조 표준화 (`domain/controller`, `domain/service`, `domain/repository`, `domain/dto`)"

$t4 += New-Issue "[W4] JPA Entity 설계 및 연관관계" "business,P1" "1단계" "## 개요`n핵심 도메인 Entity를 설계하고 연관관계를 정의한다. 불필요한 양방향 관계를 지양하고 단방향 위주로 설계한다.`n`n## 설계 대상`n- `User` (사용자) / `Role` (권한) / `UserRole` (매핑)`n- `RefreshToken`  `n- `Product` (상품)`n`n## 완료 기준`n- [ ] `@OneToMany`, `@ManyToOne` 연관관계가 N+1 없이 동작한다`n- [ ] `ddl-auto=validate`에서 스키마 검증이 통과한다"

$t4 += New-Issue "[W4] BaseEntity 상속 구현" "business,P1" "1단계" "## 개요`n모든 Entity에 공통 필드(생성일시, 수정일시, 생성자, 수정자)를 자동으로 적용한다.`n`n## 주요 작업`n- BaseEntity (@MappedSuperclass) 구현`n- @EntityListeners(AuditingEntityListener.class) 적용`n- @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy"

$t4 += New-Issue "[W4] DB 마이그레이션 (Flyway) 적용" "business,P1" "1단계" "## 개요`nFlyway로 DB 스키마 변경 이력을 버전 관리한다. 팀원 간 스키마 불일치 및 운영 배포 시 스키마 누락을 방지한다.`n`n## 주요 작업`n- `V1__init_schema.sql` 초기 스키마 작성`n- 환경별 Flyway 설정 (local: `clean` 허용, prod: 금지)`n- 마이그레이션 파일 네이밍 규칙 정의 (`V{version}__{description}.sql`)"

$t4 += New-Issue "[W4] 트랜잭션 관리" "business,P1" "1단계" "## 개요`n서비스 레이어에서 트랜잭션 경계를 명확히 정의하고 JPA와 MyBatis가 동일 트랜잭션에서 동작하도록 구성한다.`n`n## 주요 작업`n- `@Transactional(readOnly=true)` 조회 메서드 적용`n- 쓰기 트랜잭션에서 JPA flush 타이밍 확인`n- `DataSourceTransactionManager` vs `JpaTransactionManager` 선택"

$t4 += New-Issue "[W4] auth-svc 비즈니스 로직" "business,in-progress,P1" "1단계" "## 개요`nauth-svc의 핵심 비즈니스 로직(로그인, 권한 관리, 사용자 관리)을 완성한다.`n`n## 구현 범위`n- 로그인: 비밀번호 SHA-256 검증, JWT 발급`n- 토큰 재발급: RefreshToken 검증 및 Rotation`n- 사용자 조회 / 수정`n- 권한 부여 / 회수`n`n## 완료 기준`n- [ ] Postman으로 로그인 → 토큰 획득 → product-svc 호출이 정상 동작한다"

$t4 += New-Issue "[W4] product-svc 비즈니스 로직" "business,in-progress,P1" "1단계" "## 개요`nproduct-svc의 핵심 비즈니스 로직(상품 조회, 상품 관리)을 완성한다.`n`n## 구현 범위`n- 상품 목록 조회 (필터: `useYn`, `productNm`)`n- 상품 단건 조회`n- 상품 등록 / 수정 / 삭제 (관리자 권한)`n`n## 완료 기준`n- [ ] JWT 인증 후 상품 API가 정상 동작한다`n- [ ] 비활성 상품 조회 시 `-5301` 에러가 반환된다"

$t4 += New-Issue "[W4] 비즈니스 공통 기능" "business,P1" "1단계" "## 개요`n여러 서비스에서 공통으로 사용하는 비즈니스 유틸을 `libs/common`에 추가한다.`n`n## 구현 범위`n- 페이징 응답 래퍼 (`PageResponse<T>`)`n- 날짜/시간 포맷 유틸`n- 암복호화 유틸 (AES-256)"

Close-Issue $t4[0]

# ============================================================
# 2단계 Epic
# ============================================================
Write-Host "  [2단계] Epic" -ForegroundColor Magenta
$e2 = New-Epic "[2단계] MSA 안정화 및 운영 기반" "## 목표`n7/28 - 8/30 동안 MSA 패턴으로 서비스 안정성을 확보하고 CI/CD, 테스트, 모니터링 운영 기반을 구축한다.`n`n## 주차별 계획`n- **W5** Circuit Breaker, Rate Limiting 등 MSA 안정성`n- **W6** 보안 2차 강화 및 외부 연동`n- **W7** Docker / CI/CD 빌드 배포 자동화`n- **W8** 테스트 자동화 및 코드 품질`n- **W9** 로깅, 분산 추적, 모니터링 대시보드"

# W5
Write-Host "  W5" -ForegroundColor DarkCyan
$w5 = New-Issue "[W5] MSA 안정성 (7/28 ~ 8/3)" "infra,P2" "2단계" "## 목표`nMSA 환경에서 장애 전파를 차단하고 서비스 간 통신을 안정적으로 구성한다.`n`n## 완료 기준`n- [ ] 하나의 서비스 장애가 다른 서비스에 전파되지 않는다`n- [ ] 트래픽 과부하 시 Rate Limiting이 동작한다"

$t5 = @()
$t5 += New-Issue "[W5] Circuit Breaker (Resilience4j)" "infra,P2" "2단계" "## 개요`nResiilience4j Circuit Breaker를 적용하여 외부 서비스 장애 시 빠른 실패(Fail Fast)로 장애 전파를 차단한다.`n`n## 주요 작업`n- `spring-cloud-starter-circuitbreaker-resilience4j` 의존성 추가`n- Circuit Breaker 설정 (실패율 임계값, 대기 시간, half-open 설정)`n- 서비스 간 FeignClient 호출에 Circuit Breaker 적용`n`n## 완료 기준`n- [ ] 대상 서비스 다운 시 Circuit이 OPEN되고 Fallback이 호출된다`n- [ ] 서비스 복구 후 HALF-OPEN → CLOSED로 전환된다"

$t5 += New-Issue "[W5] Fallback 처리" "infra,P2" "2단계" "## 개요`nCircuit Breaker가 OPEN 상태이거나 타임아웃 발생 시 Fallback 메서드를 호출하여 기본 응답을 반환한다.`n`n## 주요 작업`n- `@CircuitBreaker(fallbackMethod=...)` 어노테이션 적용`n- Fallback 메서드 구현 (캐싱된 응답 또는 기본값 반환)`n- Fallback 발생 시 알림/로깅 처리`n`n## 완료 기준`n- [ ] 서비스 장애 시 Fallback이 호출되어 503 대신 의미 있는 응답이 반환된다"

$t5 += New-Issue "[W5] Rate Limiting (Gateway)" "infra,P2" "2단계" "## 개요`nGateway에서 IP 또는 사용자별 요청 횟수를 제한하여 API 남용을 방지한다.`n`n## 주요 작업`n- `RequestRateLimiter` 필터 적용 (Redis 기반 Token Bucket)`n- IP별 요청 제한 설정 (예: 초당 10회)`n- 제한 초과 시 `429 Too Many Requests` 응답`n`n## 완료 기준`n- [ ] 설정된 임계값 초과 요청 시 429가 반환된다"

$t5 += New-Issue "[W5] FeignClient 서비스 간 통신" "infra,P2" "2단계" "## 개요`nOpenFeign을 사용하여 서비스 간 내부 HTTP 통신을 선언적으로 구현한다.`n`n## 주요 작업`n- `spring-cloud-starter-openfeign` 의존성 추가`n- `@FeignClient(name=..., url=...)` 인터페이스 정의`n- Eureka 기반 서비스 탐색 설정`n- FeignClient에 Circuit Breaker, 타임아웃 설정`n`n## 완료 기준`n- [ ] auth-svc에서 FeignClient로 다른 서비스를 호출할 수 있다"

# W6
Write-Host "  W6" -ForegroundColor DarkCyan
$w6 = New-Issue "[W6] 보안 2차 + 외부연동 (8/4 ~ 8/10)" "security,P2" "2단계" "## 목표`n민감정보 보호, OAUTH2 연동 검토, 외부 API 연동, Redis 캐시를 구성한다."

$t6 = @()
$t6 += New-Issue "[W6] 민감정보 보호 (암복호화)" "security,P2" "2단계" "## 개요`nDB에 저장되는 민감 정보(주민번호, 계좌번호 등)를 AES-256으로 암복호화한다.`n`n## 주요 작업`n- `CryptoUtil` 구현 (AES-256-CBC)`n- JPA `AttributeConverter`로 암복호화 자동 처리`n- 암호화 키 환경변수로 관리 (Secret Management 연동)`n`n## 완료 기준`n- [ ] DB에는 암호화된 값이, 응답에는 복호화된 값이 반환된다"

$t6 += New-Issue "[W6] OAUTH2 / OpenID Connect 연동 검토" "security,P2" "2단계" "## 개요`n소셜 로그인(카카오, 네이버) 또는 사내 IdP 연동 가능성을 검토하고 프로토타입을 구현한다.`n`n## 주요 작업`n- `spring-security-oauth2-client` 적용 검토`n- 인증 서버 연동 플로우 설계`n- 기존 JWT 발급 체계와의 통합 방안 설계`n`n## 완료 기준`n- [ ] OAUTH2 연동 방안 문서 작성 완료"

$t6 += New-Issue "[W6] 외부 API 연동" "api,P2" "2단계" "## 개요`nERP 시스템 등 외부 시스템과의 HTTP 연동을 구현한다. 외부 API 장애 시 서킷 브레이커로 보호한다.`n`n## 주요 작업`n- `RestTemplate` / `WebClient` 기반 외부 API 클라이언트 구현`n- 요청/응답 로깅 인터셉터 추가`n- 타임아웃, 재시도(Retry) 정책 설정`n- Circuit Breaker 연동`n`n## 완료 기준`n- [ ] 외부 API 호출이 정상 동작하고 장애 시 Fallback이 동작한다"

$t6 += New-Issue "[W6] Redis 분산 캐시 구성 및 정책" "infra,P2" "2단계" "## 개요`nRedis를 분산 캐시로 구성하여 DB 조회 부하를 줄이고 Rate Limiting, Idempotency에도 활용한다.`n`n## 주요 작업`n- `spring-data-redis` 의존성 추가`n- `RedisConfig` 구성 (Lettuce 기반)`n- `@Cacheable`, `@CacheEvict` 어노테이션 적용`n- 캐시 키 설계 및 TTL 정책 수립`n`n## 완료 기준`n- [ ] 동일 조회 2회 호출 시 두 번째는 Redis에서 응답된다"

# W7
Write-Host "  W7" -ForegroundColor DarkCyan
$w7 = New-Issue "[W7] 빌드 / 배포 (8/11 ~ 8/17)" "build,P2" "2단계" "## 목표`nDocker 컨테이너화, 시크릿 관리, Gradle 최적화, CI/CD 파이프라인을 구성한다."

$t7 = @()
$t7 += New-Issue "[W7] Docker 컨테이너 배포 구성" "build,P2" "2단계" "## 개요`n각 서비스를 Docker 이미지로 빌드하고 컨테이너로 배포한다.`n`n## 주요 작업`n- 서비스별 `Dockerfile` 작성 (멀티스테이지 빌드)`n- `docker-compose.yml` 로컬 개발 환경 구성`n- 이미지 태그 전략 수립 (Git commit hash 또는 semantic versioning)`n- 컨테이너 헬스체크 설정`n`n## 완료 기준`n- [ ] `docker-compose up`으로 전체 서비스 기동이 가능하다"

$t7 += New-Issue "[W7] Secret Management" "security,P2" "2단계" "## 개요`nDB 비밀번호, JWT 시크릿 키 등 민감 정보를 코드와 분리하여 안전하게 관리한다.`n`n## 주요 작업`n- 환경변수 기반 시크릿 주입 방식 도입`n- `application.yml`에서 `${ENV_VAR}` 참조`n- CI/CD 파이프라인에서 시크릿 주입 방법 정의`n- 개발팀 시크릿 공유 방식 수립 (1Password, Vault 등 검토)"

$t7 += New-Issue "[W7] Gradle 빌드 최적화" "build,P2" "2단계" "## 개요`nGradle 빌드 캐시, 병렬 빌드 등을 적용하여 빌드 시간을 단축한다.`n`n## 주요 작업`n- Gradle Build Cache 활성화`n- `--parallel` 빌드 적용`n- 불필요한 의존성 제거`n- `gradle.properties` 힙 메모리 튜닝`n`n## 완료 기준`n- [ ] 증분 빌드 시간이 최초 빌드 대비 50% 이상 단축된다"

$t7 += New-Issue "[W7] CI/CD 파이프라인 구성" "build,P2" "2단계" "## 개요`nGitHub Actions로 push 시 자동 빌드/테스트/배포 파이프라인을 구성한다.`n`n## 파이프라인 단계`n1. 코드 체크아웃`n2. Gradle 빌드 및 테스트`n3. Docker 이미지 빌드 및 레지스트리 푸시`n4. 개발 서버 배포 (`deploy-dev.yml`)`n`n## 완료 기준`n- [ ] `develop` 브랜치 push 시 자동 배포가 완료된다"

# W8
Write-Host "  W8" -ForegroundColor DarkCyan
$w8 = New-Issue "[W8] 테스트 / 품질 (8/18 ~ 8/24)" "test,P2" "2단계" "## 목표`n코드 품질 기준을 수립하고 단위 테스트, 통합 테스트를 작성하여 회귀 방지 체계를 갖춘다."

$t8 = @()
$t8 += New-Issue "[W8] 코드스타일 및 품질관리 (Checkstyle)" "test,P2" "2단계" "## 개요`nCheckstyle / SpotBugs를 사용하여 코드 스타일을 통일하고 잠재적 버그를 사전에 탐지한다.`n`n## 주요 작업`n- Checkstyle 규칙 파일 작성 (Google Style 기반 커스터마이징)`n- Gradle Checkstyle 플러그인 설정`n- SpotBugs 정적 분석 적용`n- CI 파이프라인에서 품질 검사 실패 시 빌드 중단"

$t8 += New-Issue "[W8] 단위 테스트 작성" "test,P2" "2단계" "## 개요`nJUnit 5 + Mockito로 서비스 레이어 단위 테스트를 작성한다. 외부 의존성은 Mock으로 대체한다.`n`n## 커버리지 목표`n- Service 레이어: 80% 이상`n`n## 주요 작업`n- `@ExtendWith(MockitoExtension.class)` 기반 테스트 작성`n- `AuthService`, `ProductService` 핵심 로직 테스트`n- 경계 조건 및 예외 케이스 테스트"

$t8 += New-Issue "[W8] 통합 테스트 / 연계 테스트" "test,P2" "2단계" "## 개요`n실제 DB와 연동하여 Controller → Service → Repository 전체 흐름을 테스트한다.`n`n## 주요 작업`n- `@SpringBootTest` + `@AutoConfigureMockMvc` 기반 통합 테스트`n- Testcontainers로 PostgreSQL 컨테이너 사용`n- 인증 흐름 (로그인 → 토큰 발급 → API 호출) E2E 테스트`n`n## 완료 기준`n- [ ] CI에서 통합 테스트가 자동 실행된다"

# W9
Write-Host "  W9" -ForegroundColor DarkCyan
$w9 = New-Issue "[W9] 운영 / 모니터링 (8/25 ~ 8/30)" "monitoring,P2" "2단계" "## 목표`n로깅 표준화, 분산 추적, 헬스체크, 모니터링 대시보드를 구성하여 운영 가시성을 확보한다."

$t9 = @()
$t9 += New-Issue "[W9] Logback JSON 로깅 설정" "monitoring,P2" "2단계" "## 개요`n모든 서비스 로그를 JSON 포맷으로 출력하여 ELK Stack 수집을 용이하게 한다.`n`n## 주요 작업`n- `logstash-logback-encoder` 의존성 추가`n- `logback-spring.xml` JSON 인코더 설정`n- 로그 필드 표준화 (timestamp, level, service, traceId, message)`n- 환경별 로그 레벨 설정 (local: DEBUG, prod: INFO)"

$t9 += New-Issue "[W9] 분산 추적 (Micrometer + Zipkin)" "monitoring,P2" "2단계" "## 개요`nMicrometer Tracing과 Zipkin으로 서비스 간 요청 흐름을 추적한다. 지연이 발생하는 서비스를 빠르게 식별한다.`n`n## 주요 작업`n- `micrometer-tracing-bridge-brave`, `zipkin-reporter-brave` 의존성 추가`n- `management.tracing.sampling.probability=1.0` 설정`n- Zipkin 서버 구성 및 대시보드 확인`n- 로그에 `traceId`, `spanId` 포함"

$t9 += New-Issue "[W9] 헬스체크 및 메트릭 (Actuator)" "monitoring,P2" "2단계" "## 개요`nSpring Boot Actuator로 서비스 헬스체크 엔드포인트와 메트릭을 제공한다.`n`n## 주요 작업`n- `/actuator/health` 엔드포인트 활성화`n- DB, Eureka, Redis 헬스 인디케이터 구성`n- `/actuator/metrics`, `/actuator/prometheus` 엔드포인트 활성화`n- Gateway 헬스체크 라우트 설정"

$t9 += New-Issue "[W9] 모니터링 대시보드 구성" "monitoring,P2" "2단계" "## 개요`nPrometheus + Grafana로 서비스 메트릭을 수집하고 시각화 대시보드를 구성한다.`n`n## 주요 작업`n- Prometheus 스크레이프 설정 (각 서비스 `/actuator/prometheus`)`n- Grafana 데이터소스 연결`n- JVM 메트릭, HTTP 요청 수/지연, 에러율 대시보드 구성`n- 임계값 초과 시 알림(Alert) 설정"

# ============================================================
# 3단계 Epic
# ============================================================
Write-Host "  [3단계] Epic" -ForegroundColor Magenta
$e3 = New-Epic "[3단계] 고급 MSA + 배포 고도화" "## 목표`n9/1 - 9/28 동안 Kafka 이벤트 기반 통신, SAGA/CQRS 패턴, Config Server, 핫 디플로이 등 고급 MSA 패턴을 완성한다.`n`n## 주차별 계획`n- **W10** Kafka, SAGA, CQRS 고급 MSA 패턴`n- **W11** Spring Cloud Config 설정 고도화`n- **W12** Rolling Update(핫 디플로이), Blue/Green, Canary 배포 고도화`n- **W13** ELK, 부하 테스트, Contract Testing, SLO 운영 고도화"

# W10
Write-Host "  W10" -ForegroundColor DarkCyan
$w10 = New-Issue "[W10] 고급 MSA 패턴 (9/1 ~ 9/7)" "infra,P3" "3단계" "## 목표`nKafka 기반 이벤트 드리븐 아키텍처, SAGA 패턴 분산 트랜잭션, CQRS를 구현한다."

$t10 = @()
$t10 += New-Issue "[W10] 이벤트 기반 통신 (Kafka)" "infra,P3" "3단계" "## 개요`nApache Kafka를 사용하여 서비스 간 비동기 이벤트 통신을 구현한다. 동기 REST 호출의 강결합 문제를 해소한다.`n`n## 주요 작업`n- `spring-kafka` 의존성 추가`n- Producer / Consumer 구성`n- 토픽 설계 (도메인 이벤트 기반: `user.created`, `order.placed` 등)`n- 메시지 직렬화 (JSON / Avro 검토)`n- Dead Letter Queue 처리"

$t10 += New-Issue "[W10] 분산 트랜잭션 (SAGA 패턴)" "infra,P3" "3단계" "## 개요`n여러 서비스에 걸친 분산 트랜잭션을 SAGA 패턴으로 처리한다. 로컬 트랜잭션과 보상 트랜잭션으로 데이터 일관성을 유지한다.`n`n## 주요 작업`n- Choreography 기반 SAGA 설계 (Kafka 이벤트 체인)`n- 보상 트랜잭션 구현 (롤백 이벤트 발행)`n- SAGA 상태 관리 테이블 설계"

$t10 += New-Issue "[W10] CQRS 설계 및 구현" "infra,P3" "3단계" "## 개요`n명령(Command)과 조회(Query)의 책임을 분리하여 읽기 성능을 최적화하고 복잡한 쿼리를 단순화한다.`n`n## 주요 작업`n- Command Model: JPA 기반 도메인 로직 처리`n- Query Model: MyBatis 기반 복잡한 조회 처리`n- 이벤트 소싱으로 Query Model 동기화 (Kafka 활용)"

# W11
Write-Host "  W11" -ForegroundColor DarkCyan
$w11 = New-Issue "[W11] 설정 고도화 (9/8 ~ 9/14)" "infra,P3" "3단계" "## 목표`nSpring Cloud Config로 설정을 중앙화하고 재배포 없이 실시간 설정 변경이 가능하도록 한다."

$t11 = @()
$t11 += New-Issue "[W11] Spring Cloud Config Server 구성" "infra,P3" "3단계" "## 개요`nSpring Cloud Config Server로 모든 서비스의 설정을 Git 레포지터리에서 중앙 관리한다.`n`n## 주요 작업`n- `config-server` 모듈 추가`n- Git 백엔드 연결 (설정 레포지터리)`n- 각 서비스에 Config Client 적용`n- 환경별 설정 파일 분리 (`application-{profile}.yml`)"

$t11 += New-Issue "[W11] 실시간 설정 변경 (Actuator refresh)" "infra,P3" "3단계" "## 개요`n설정 변경 후 서비스 재시작 없이 `@RefreshScope` 빈을 갱신한다. Spring Cloud Bus로 전체 인스턴스에 일괄 전파한다.`n`n## 주요 작업`n- `@RefreshScope` 어노테이션 적용`n- `/actuator/refresh` 엔드포인트 활성화`n- Spring Cloud Bus + Kafka로 전체 서비스 설정 동시 갱신"

# W12
Write-Host "  W12" -ForegroundColor DarkCyan
$w12 = New-Issue "[W12] 배포 고도화 - 핫 디플로이 (9/15 ~ 9/21)" "build,P3" "3단계" "## 목표`n무중단 배포 전략(Rolling Update, Blue/Green, Canary)을 구현하고 롤백 자동화로 배포 안정성을 확보한다."

$t12 = @()
$t12 += New-Issue "[W12] Rolling Update (핫 디플로이)" "build,P3" "3단계" "## 개요`n기존 인스턴스를 순차적으로 새 버전으로 교체하여 서비스 중단 없이 배포한다. Kubernetes Deployment의 기본 배포 전략이다.`n`n## 주요 작업`n- `maxSurge`, `maxUnavailable` 설정`n- Readiness Probe / Liveness Probe 구성`n- Graceful Shutdown과 연동하여 종료 전 트래픽 차단`n- 배포 중 헬스체크 모니터링`n`n## 완료 기준`n- [ ] 배포 중 사용자 요청이 유실되지 않는다"

$t12 += New-Issue "[W12] Blue / Green 배포 구성" "build,P3" "3단계" "## 개요`nBlue(현재)와 Green(신규) 두 환경을 운영하여 트래픽을 순간 전환한다. 롤백이 즉시 가능하다.`n`n## 주요 작업`n- Blue/Green 환경 분리 구성`n- Gateway 또는 로드밸런서에서 트래픽 전환`n- 전환 후 Blue 환경 유지 (롤백 대비)`n`n## 완료 기준`n- [ ] 트래픽 전환이 다운타임 없이 이루어진다"

$t12 += New-Issue "[W12] Canary 배포 전략 적용" "build,P3" "3단계" "## 개요`n새 버전을 일부 트래픽(5~10%)에만 먼저 배포하여 검증 후 전체 롤아웃한다.`n`n## 주요 작업`n- Gateway에서 헤더 또는 비율 기반 트래픽 분할`n- Canary 버전 메트릭 모니터링 (에러율, 응답시간)`n- 이상 감지 시 자동 롤백 또는 수동 롤아웃 중단"

$t12 += New-Issue "[W12] 롤백 자동화" "build,P3" "3단계" "## 개요`n배포 후 에러율 또는 응답 시간이 임계값을 초과하면 자동으로 이전 버전으로 롤백한다.`n`n## 주요 작업`n- Prometheus 알림 기반 롤백 트리거 설정`n- GitHub Actions에 롤백 워크플로우 추가`n- 롤백 알림 (Slack 등)"

# W13
Write-Host "  W13" -ForegroundColor DarkCyan
$w13 = New-Issue "[W13] 운영 고도화 + 마무리 (9/22 ~ 9/28)" "monitoring,P3" "3단계" "## 목표`nELK 로그 중앙화, 부하 테스트, Contract Testing, 성능 최적화, SLO/SLA를 정의하여 프로젝트를 마무리한다."

$t13 = @()
$t13 += New-Issue "[W13] ELK Stack 로그 중앙화" "monitoring,P3" "3단계" "## 개요`nElasticsearch + Logstash + Kibana로 모든 서비스 로그를 중앙 수집하고 검색/분석한다.`n`n## 주요 작업`n- Logstash 파이프라인 설정 (Filebeat → Logstash → Elasticsearch)`n- Kibana 대시보드 구성 (에러 로그, 요청 로그, 서비스별 집계)`n- 로그 보존 기간 정책 설정 (ILM: Index Lifecycle Management)"

$t13 += New-Issue "[W13] 부하 테스트 (k6)" "test,P3" "3단계" "## 개요`nk6로 API 부하 테스트를 수행하여 성능 병목을 발견하고 목표 TPS를 검증한다.`n`n## 주요 작업`n- k6 스크립트 작성 (로그인 → 상품 조회 시나리오)`n- 단계별 부하 증가 (Ramp-up) 시나리오 구성`n- 목표 TPS 설정 및 통과 기준 정의`n- 결과 리포트 생성 및 병목 분석"

$t13 += New-Issue "[W13] Contract Testing (Pact)" "test,P3" "3단계" "## 개요`nPact를 사용하여 소비자(Consumer)와 공급자(Provider) 간 API 계약을 테스트한다. 프론트엔드와의 계약 위반을 사전에 감지한다.`n`n## 주요 작업`n- Consumer 측 Pact 테스트 작성`n- Provider 측 Pact 검증 테스트 작성`n- Pact Broker 구성 및 CI 연동"

$t13 += New-Issue "[W13] 성능 최적화 (쿼리튜닝, HikariCP)" "business,P3" "3단계" "## 개요`n부하 테스트 결과를 기반으로 쿼리 최적화, 커넥션 풀 튜닝을 수행한다.`n`n## 주요 작업`n- N+1 쿼리 제거 (`@EntityGraph`, Fetch Join 적용)`n- 인덱스 최적화 (`EXPLAIN ANALYZE` 분석)`n- HikariCP 커넥션 풀 크기 튜닝`n- Redis 캐시 적중률 개선"

$t13 += New-Issue "[W13] SLO / SLA 정의" "monitoring,P3" "3단계" "## 개요`n서비스 수준 목표(SLO)와 협약(SLA)을 정의하고 Grafana 대시보드에서 실시간 달성 여부를 모니터링한다.`n`n## SLO 예시`n| 지표 | 목표 |`n|------|------|`n| 가용성 | 99.9% |`n| 응답 시간 (P95) | 200ms 이하 |`n| 에러율 | 0.1% 이하 |`n`n## 주요 작업`n- Prometheus + Grafana SLO 대시보드 구성`n- Error Budget 계산 및 알림 설정"

# ── 5. Sub-issue 연결 ──────────────────────────────────────────────

Write-Host "[5/5] Linking sub-issues..." -ForegroundColor Yellow

# 1단계
Add-Sub $e1 $w1; Add-Sub $e1 $w2; Add-Sub $e1 $w3; Add-Sub $e1 $w4
foreach ($n in $t)  { Add-Sub $w1 $n }
foreach ($n in $t2) { Add-Sub $w2 $n }
foreach ($n in $t3) { Add-Sub $w3 $n }
foreach ($n in $t4) { Add-Sub $w4 $n }

# 2단계
Add-Sub $e2 $w5; Add-Sub $e2 $w6; Add-Sub $e2 $w7; Add-Sub $e2 $w8; Add-Sub $e2 $w9
foreach ($n in $t5) { Add-Sub $w5 $n }
foreach ($n in $t6) { Add-Sub $w6 $n }
foreach ($n in $t7) { Add-Sub $w7 $n }
foreach ($n in $t8) { Add-Sub $w8 $n }
foreach ($n in $t9) { Add-Sub $w9 $n }

# 3단계
Add-Sub $e3 $w10; Add-Sub $e3 $w11; Add-Sub $e3 $w12; Add-Sub $e3 $w13
foreach ($n in $t10) { Add-Sub $w10 $n }
foreach ($n in $t11) { Add-Sub $w11 $n }
foreach ($n in $t12) { Add-Sub $w12 $n }
foreach ($n in $t13) { Add-Sub $w13 $n }

# ── 6. 프로젝트 날짜 설정 (로드맵) ───────────────────────────────────

Write-Host "[6/6] Setting roadmap dates..." -ForegroundColor Yellow

$StartFieldId = "PVTF_lAHOAiImm84BbakkzhWMSws"
$EndFieldId   = "PVTF_lAHOAiImm84BbakkzhWMSww"

# 프로젝트 아이템 ID 캐시 로드
$ItemCache = @{}
$iq = 'query($o:String!,$n:Int!){user(login:$o){projectV2(number:$n){items(first:100){nodes{id content{...on Issue{number}}}}}}}'
$ir = gh api graphql --field "query=$iq" --field "o=$Owner" --field "n=2" | ConvertFrom-Json
foreach ($node in $ir.data.user.projectV2.items.nodes) {
    if ($node.content.number) { $ItemCache[$node.content.number] = $node.id }
}
Write-Host "  Loaded $($ItemCache.Count) project items"

function Set-RoadmapDate($issueNum, $start, $end) {
    $itemId = $ItemCache[$issueNum]
    if (-not $itemId) {
        # 프로젝트에 없으면 추가 후 재로드
        gh project item-add 2 --owner $Owner --url "https://github.com/$Repo/issues/$issueNum" | Out-Null
        $ir2 = gh api graphql --field "query=$iq" --field "o=$Owner" --field "n=2" | ConvertFrom-Json
        foreach ($node in $ir2.data.user.projectV2.items.nodes) {
            if ($node.content.number) { $ItemCache[$node.content.number] = $node.id }
        }
        $itemId = $ItemCache[$issueNum]
    }
    if (-not $itemId) { Write-Host "  !! #$issueNum not in project" -ForegroundColor Red; return }
    $dm = 'mutation($proj:ID!,$item:ID!,$fid:ID!,$v:Date!){updateProjectV2ItemFieldValue(input:{projectId:$proj,itemId:$item,fieldId:$fid,value:{date:$v}}){projectV2Item{id}}}'
    gh api graphql --field "query=$dm" --field "proj=$ProjId" --field "item=$itemId" --field "fid=$StartFieldId" --field "v=$start" | Out-Null
    gh api graphql --field "query=$dm" --field "proj=$ProjId" --field "item=$itemId" --field "fid=$EndFieldId"   --field "v=$end"   | Out-Null
    Write-Host "  #$issueNum  $start ~ $end" -ForegroundColor Gray
}

# Epic + Week 날짜
Set-RoadmapDate $e1  "2026-07-01" "2026-07-27"
Set-RoadmapDate $w1  "2026-07-01" "2026-07-06"
Set-RoadmapDate $w2  "2026-07-07" "2026-07-13"
Set-RoadmapDate $w3  "2026-07-14" "2026-07-20"
Set-RoadmapDate $w4  "2026-07-21" "2026-07-27"

Set-RoadmapDate $e2  "2026-07-28" "2026-08-30"
Set-RoadmapDate $w5  "2026-07-28" "2026-08-03"
Set-RoadmapDate $w6  "2026-08-04" "2026-08-10"
Set-RoadmapDate $w7  "2026-08-11" "2026-08-17"
Set-RoadmapDate $w8  "2026-08-18" "2026-08-24"
Set-RoadmapDate $w9  "2026-08-25" "2026-08-30"

Set-RoadmapDate $e3  "2026-09-01" "2026-09-28"
Set-RoadmapDate $w10 "2026-09-01" "2026-09-07"
Set-RoadmapDate $w11 "2026-09-08" "2026-09-14"
Set-RoadmapDate $w12 "2026-09-15" "2026-09-21"
Set-RoadmapDate $w13 "2026-09-22" "2026-09-28"

# Task 날짜 (소속 주차와 동일)
$taskDateMap = @(
    @{ nums = $t;   s = "2026-07-01"; e = "2026-07-06" }
    @{ nums = $t2;  s = "2026-07-07"; e = "2026-07-13" }
    @{ nums = $t3;  s = "2026-07-14"; e = "2026-07-20" }
    @{ nums = $t4;  s = "2026-07-21"; e = "2026-07-27" }
    @{ nums = $t5;  s = "2026-07-28"; e = "2026-08-03" }
    @{ nums = $t6;  s = "2026-08-04"; e = "2026-08-10" }
    @{ nums = $t7;  s = "2026-08-11"; e = "2026-08-17" }
    @{ nums = $t8;  s = "2026-08-18"; e = "2026-08-24" }
    @{ nums = $t9;  s = "2026-08-25"; e = "2026-08-30" }
    @{ nums = $t10; s = "2026-09-01"; e = "2026-09-07" }
    @{ nums = $t11; s = "2026-09-08"; e = "2026-09-14" }
    @{ nums = $t12; s = "2026-09-15"; e = "2026-09-21" }
    @{ nums = $t13; s = "2026-09-22"; e = "2026-09-28" }
)
foreach ($entry in $taskDateMap) {
    foreach ($n in $entry.nums) {
        if ($n -gt 0) { Set-RoadmapDate $n $entry.s $entry.e }
    }
}

Write-Host "Done! https://github.com/$Repo/issues" -ForegroundColor Green
