# ================================================================
# bx-cf-be GitHub Projects WBS 일괄 생성 스크립트 (계층 구조)
#
# 이슈 구조:
#   [1단계] Epic
#     └─ [W1] 주차 이슈 (서브이슈)
#          └─ 태스크 (서브이슈의 서브이슈)
#
# 사전 준비:
#   1. scoop install gh
#   2. gh auth login
#   3. gh auth refresh -s read:project
#
# 최초 1회 - 파일 인코딩 UTF-8 BOM 변환 (한글 깨짐 방지):
#   $p = "C:\proj\bx-cf-be\scripts\github-projects-setup.ps1"
#   $c = [System.IO.File]::ReadAllText($p, [System.Text.Encoding]::UTF8)
#   [System.IO.File]::WriteAllText($p, $c, (New-Object System.Text.UTF8Encoding $true))
#
# 재실행 시 기존 이슈 삭제:
#   gh issue list --repo "yooyongbeom/bx-cf-be" --limit 100 --json number |
#     ConvertFrom-Json |
#     ForEach-Object { gh issue delete $_.number --repo "yooyongbeom/bx-cf-be" --yes }
#
# 실행:
#   .\scripts\github-projects-setup.ps1 -Repo "yooyongbeom/bx-cf-be"
# ================================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$Repo
)

# 이슈 생성 후 번호 반환
function New-Issue($title, $labels, $milestone, $body) {
    Write-Host "    ➜ $title" -ForegroundColor Cyan
    $url = gh issue create --repo $Repo `
        --title $title `
        --label $labels `
        --milestone $milestone `
        --body $body
    return [int]($url.Trim() -split '/')[-1]
}

# 에픽 이슈 생성 (마일스톤 없음)
function New-Epic($title, $label, $body) {
    Write-Host "  ★ $title" -ForegroundColor Yellow
    $url = gh issue create --repo $Repo `
        --title $title `
        --label $label `
        --body $body
    return [int]($url.Trim() -split '/')[-1]
}

# 주차 이슈 생성
function New-Week($title, $labels, $milestone, $body) {
    Write-Host "  ▶ $title" -ForegroundColor Magenta
    $url = gh issue create --repo $Repo `
        --title $title `
        --label $labels `
        --milestone $milestone `
        --body $body
    return [int]($url.Trim() -split '/')[-1]
}

# 서브이슈 연결
function Add-SubIssue($parentNum, $childNum) {
    gh api repos/$Repo/issues/$parentNum/sub_issues `
        -f sub_issue_id="$childNum" | Out-Null
}

# ================================================================
# 1. Milestone 생성
# ================================================================
Write-Host "`n[1/4] Milestone 생성 중..." -ForegroundColor Yellow

gh api repos/$Repo/milestones -f title="1단계" -f due_on="2026-07-27T00:00:00Z" -f description="Phase 1: 7/1 - 7/27" | Out-Null
gh api repos/$Repo/milestones -f title="2단계" -f due_on="2026-08-30T00:00:00Z" -f description="Phase 2: 7/28 - 8/30" | Out-Null
gh api repos/$Repo/milestones -f title="3단계" -f due_on="2026-09-28T00:00:00Z" -f description="Phase 3: 9/1 - 9/28" | Out-Null

Write-Host "  ✅ Milestone 3개 생성 완료" -ForegroundColor Green

# ================================================================
# 2. Label 생성
# ================================================================
Write-Host "`n[2/4] Label 생성 중..." -ForegroundColor Yellow

$labelDefs = @(
    @{ name="infra";      color="0075ca"; desc="인프라 구성" },
    @{ name="security";   color="d93f0b"; desc="보안" },
    @{ name="api";        color="0e8a16"; desc="API 설계 및 문서화" },
    @{ name="business";   color="e4e669"; desc="비즈니스 로직" },
    @{ name="test";       color="cc317c"; desc="테스트 / 품질" },
    @{ name="monitoring"; color="1d76db"; desc="운영 / 모니터링" },
    @{ name="build";      color="5319e7"; desc="빌드 / 배포" },
    @{ name="epic";       color="000000"; desc="에픽 (단계)" },
    @{ name="P1";         color="ff0000"; desc="우선순위 높음 (1단계)" },
    @{ name="P2";         color="ff9900"; desc="우선순위 중간 (2단계)" },
    @{ name="P3";         color="6699cc"; desc="우선순위 낮음 (3단계)" }
)

foreach ($l in $labelDefs) {
    gh label create $l.name --color $l.color --description $l.desc --repo $Repo 2>$null
    Write-Host "  ➜ $($l.name)" -ForegroundColor Cyan
}
Write-Host "  ✅ Label 생성 완료" -ForegroundColor Green

# ================================================================
# 3. Epic + 주차 + 태스크 이슈 생성 (계층 구조)
# ================================================================
Write-Host "`n[3/4] Issues 생성 중 (Epic → 주차 → 태스크)..." -ForegroundColor Yellow

# ──────────────────────────────────────────────────────────────────
# 1단계 Epic
# ──────────────────────────────────────────────────────────────────
Write-Host "`n[1단계] 기초 구조 완성" -ForegroundColor Green
$epic1 = New-Epic "[1단계] 기초 구조 완성" "epic,P1" "7/1 - 7/27 기초 인프라 및 핵심 기능 완성"

# W1
$w1 = New-Week "[W1] 인프라 마무리 (7/1 ~ 7/6)" "infra,P1" "1단계" "Gateway 인증 필터, CORS, Graceful Shutdown"
Add-SubIssue $epic1 $w1
$t = New-Issue "[W1] Gateway 인증 필터 (JWT 검증)" "infra,P1" "1단계" "Spring Cloud Gateway에서 JWT를 검증하는 GlobalFilter 구현`n- GlobalFilter 구현`n- 토큰 만료/변조 예외 처리`n- 인증 불필요 경로 화이트리스트"
Add-SubIssue $w1 $t
$t = New-Issue "[W1] Gateway CORS 설정" "infra,P1" "1단계" "Gateway 레벨 CORS 정책 설정`n- 허용 Origin / Method / Header 정의`n- Preflight 요청 처리"
Add-SubIssue $w1 $t
$t = New-Issue "[W1] Graceful Shutdown 구현" "infra,P1" "1단계" "배포 시 진행 중인 요청을 완료한 후 종료`n- server.shutdown=graceful 설정`n- spring.lifecycle.timeout-per-shutdown-phase 설정"
Add-SubIssue $w1 $t

# W2
$w2 = New-Week "[W2] 보안 1차 마무리 (7/7 ~ 7/13)" "security,P1" "1단계" "에러 코드 표준화, 권한 도메인 설계, 권한 API"
Add-SubIssue $epic1 $w2
$t = New-Issue "[W2] 에러 코드 체계 표준화" "security,P1" "1단계" "서비스 전반에서 사용하는 에러 코드 통일`n- 공통 ErrorCode enum 설계`n- libs/common에 위치"
Add-SubIssue $w2 $t
$t = New-Issue "[W2] 권한 도메인 설계" "security,P1" "1단계" "역할(Role) 및 권한(Permission) 도메인 모델 설계`n- Role / Permission 관계 정의`n- DB 스키마 설계"
Add-SubIssue $w2 $t
$t = New-Issue "[W2] 권한 API 구현" "security,P1" "1단계" "역할 및 권한 관련 CRUD API 구현`n- 권한 조회 / 부여 / 회수"
Add-SubIssue $w2 $t

# W3
$w3 = New-Week "[W3] API 설계 마무리 (7/14 ~ 7/20)" "api,P1" "1단계" "글로벌 예외처리, Validation, Versioning, Idempotency"
Add-SubIssue $epic1 $w3
$t = New-Issue "[W3] 글로벌 예외 처리" "api,P1" "1단계" "@RestControllerAdvice 기반 전역 예외 핸들러 구현`n- BusinessException / ValidationException 분리`n- ApiResponse 형식으로 통일된 오류 응답"
Add-SubIssue $w3 $t
$t = New-Issue "[W3] Validation 적용 (@Valid)" "api,P1" "1단계" "요청 DTO에 Bean Validation 적용`n- @NotBlank / @Size / @Pattern 등`n- 검증 실패 시 표준 오류 응답"
Add-SubIssue $w3 $t
$t = New-Issue "[W3] API Versioning (/v1/) 적용" "api,P1" "1단계" "URL 기반 API 버전 관리 도입`n- /v1/ prefix 적용`n- Gateway 라우팅에 버전 반영"
Add-SubIssue $w3 $t
$t = New-Issue "[W3] Idempotency (멱등성 처리)" "api,P1" "1단계" "중복 요청 방지 처리`n- Idempotency-Key 헤더 기반`n- Redis 활용 중복 체크"
Add-SubIssue $w3 $t

# W4
$w4 = New-Week "[W4] 비즈니스 로직 기반 (7/21 ~ 7/27)" "business,P1" "1단계" "JPA Entity 설계, BaseEntity, Flyway, 트랜잭션"
Add-SubIssue $epic1 $w4
$t = New-Issue "[W4] JPA Entity 설계 및 연관관계" "business,P1" "1단계" "서비스별 JPA Entity 설계`n- 연관관계 매핑 (1:N, N:M)`n- 인덱스 설계"
Add-SubIssue $w4 $t
$t = New-Issue "[W4] BaseEntity 상속 구현" "business,P1" "1단계" "공통 감사 필드 BaseEntity 구현`n- createdAt / updatedAt / createdBy / updatedBy`n- @EntityListeners(AuditingEntityListener.class)"
Add-SubIssue $w4 $t
$t = New-Issue "[W4] DB 마이그레이션 (Flyway) 적용" "business,P1" "1단계" "Flyway 기반 스키마 버전 관리`n- V1__init.sql 작성`n- 환경별 마이그레이션 전략"
Add-SubIssue $w4 $t
$t = New-Issue "[W4] 트랜잭션 관리" "business,P1" "1단계" "서비스 레이어 트랜잭션 전략 수립`n- @Transactional 적용 기준 정의`n- readOnly 분리"
Add-SubIssue $w4 $t

# ──────────────────────────────────────────────────────────────────
# 2단계 Epic
# ──────────────────────────────────────────────────────────────────
Write-Host "`n[2단계] 안정화 및 운영 기반" -ForegroundColor Green
$epic2 = New-Epic "[2단계] 안정화 및 운영 기반" "epic,P2" "7/28 - 8/30 MSA 안정성 확보 및 운영 준비"

# W5
$w5 = New-Week "[W5] MSA 안정성 (7/28 ~ 8/3)" "infra,P2" "2단계" "Circuit Breaker, Rate Limiting, FeignClient"
Add-SubIssue $epic2 $w5
$t = New-Issue "[W5] Circuit Breaker (Resilience4j) 구현" "infra,P2" "2단계" "서비스 장애 전파 차단`n- Resilience4j 설정`n- 상태 모니터링 (CLOSED/OPEN/HALF_OPEN)"
Add-SubIssue $w5 $t
$t = New-Issue "[W5] Fallback 처리" "infra,P2" "2단계" "Circuit Breaker 발동 시 Fallback 응답 구현`n- 기본값 반환 또는 캐시 데이터 활용"
Add-SubIssue $w5 $t
$t = New-Issue "[W5] Rate Limiting (Gateway) 적용" "infra,P2" "2단계" "Gateway 레벨 API 요청 수 제한`n- Redis 기반 RequestRateLimiter 필터`n- 엔드포인트별 허용량 정의"
Add-SubIssue $w5 $t
$t = New-Issue "[W5] FeignClient 서비스 간 통신 구현" "infra,P2" "2단계" "서비스 간 HTTP 통신 구성`n- auth-svc ↔ product-svc 연동`n- FeignClient + CircuitBreaker 연계"
Add-SubIssue $w5 $t

# W6
$w6 = New-Week "[W6] 보안 2차 + 외부연동 (8/4 ~ 8/10)" "security,P2" "2단계" "민감정보 보호, OAUTH2, 외부 API, Redis"
Add-SubIssue $epic2 $w6
$t = New-Issue "[W6] 민감정보 보호 (암복호화)" "security,P2" "2단계" "민감 데이터 암복호화 처리`n- AES 암호화 유틸 구현`n- DB 저장 시 암호화 / 조회 시 복호화"
Add-SubIssue $w6 $t
$t = New-Issue "[W6] OAUTH2 / OpenID Connect 연동 검토" "security,P2" "2단계" "SSO 연동 필요 여부 결정 후 적용`n- Spring Security OAuth2 Client 검토"
Add-SubIssue $w6 $t
$t = New-Issue "[W6] 외부 API 연동" "api,P2" "2단계" "외부 시스템 API 연동 구현`n- HttpClient / RestTemplate / WebClient 선택`n- 연동 오류 처리 및 재시도 정책"
Add-SubIssue $w6 $t
$t = New-Issue "[W6] Redis 분산 캐시 구성 및 정책" "infra,P2" "2단계" "Redis 캐시 인프라 구성`n- Spring Cache 추상화 적용`n- TTL / Eviction 정책 정의"
Add-SubIssue $w6 $t

# W7
$w7 = New-Week "[W7] 빌드 / 배포 (8/11 ~ 8/17)" "build,P2" "2단계" "Docker, Secret Management, Gradle 최적화"
Add-SubIssue $epic2 $w7
$t = New-Issue "[W7] Docker 컨테이너 배포 구성" "build,P2" "2단계" "서비스별 Dockerfile 작성`n- 멀티스테이지 빌드`n- 환경별 이미지 태깅 전략"
Add-SubIssue $w7 $t
$t = New-Issue "[W7] 비밀 관리 (Secret Management)" "build,P2" "2단계" "민감 설정 외부화`n- GitHub Secrets 또는 Vault 연동`n- 운영 환경 환경변수 관리"
Add-SubIssue $w7 $t
$t = New-Issue "[W7] Gradle 빌드 최적화" "build,P2" "2단계" "빌드 속도 개선`n- 빌드 캐시 활용`n- 병렬 빌드 설정"
Add-SubIssue $w7 $t

# W8
$w8 = New-Week "[W8] 테스트 / 품질 (8/18 ~ 8/24)" "test,P2" "2단계" "코드스타일, 단위테스트, 통합테스트"
Add-SubIssue $epic2 $w8
$t = New-Issue "[W8] 코드스타일 및 품질관리 설정" "test,P2" "2단계" "정적 분석 및 코드 스타일 통일`n- Checkstyle / SpotBugs 설정`n- SonarQube 연동 검토"
Add-SubIssue $w8 $t
$t = New-Issue "[W8] 단위테스트 작성" "test,P2" "2단계" "서비스 레이어 단위테스트`n- JUnit 5 + Mockito`n- 커버리지 목표 80% 이상"
Add-SubIssue $w8 $t
$t = New-Issue "[W8] 통합테스트 / 연계테스트" "test,P2" "2단계" "API 레벨 통합테스트`n- @SpringBootTest 기반`n- TestContainers 활용 (DB)"
Add-SubIssue $w8 $t

# W9
$w9 = New-Week "[W9] 운영 / 모니터링 (8/25 ~ 8/30)" "monitoring,P2" "2단계" "Logback, 분산추적, Actuator, 대시보드"
Add-SubIssue $epic2 $w9
$t = New-Issue "[W9] Logback JSON 로깅 설정" "monitoring,P2" "2단계" "구조화 로그 출력`n- logstash-logback-encoder 적용`n- 요청 ID(MDC) 포함"
Add-SubIssue $w9 $t
$t = New-Issue "[W9] 분산 추적 (Micrometer + Zipkin)" "monitoring,P2" "2단계" "서비스 간 요청 흐름 추적`n- Micrometer Tracing 설정`n- Zipkin 서버 연동`n- TraceId / SpanId 로그 포함"
Add-SubIssue $w9 $t
$t = New-Issue "[W9] 헬스체크 및 메트릭 (Actuator)" "monitoring,P2" "2단계" "Spring Boot Actuator 설정`n- /actuator/health 엔드포인트`n- Prometheus 메트릭 노출"
Add-SubIssue $w9 $t
$t = New-Issue "[W9] 모니터링 대시보드 구성" "monitoring,P2" "2단계" "Grafana 대시보드 구성`n- Prometheus 데이터소스 연결`n- JVM / API / DB 메트릭 시각화"
Add-SubIssue $w9 $t

# ──────────────────────────────────────────────────────────────────
# 3단계 Epic
# ──────────────────────────────────────────────────────────────────
Write-Host "`n[3단계] 고급 MSA + 최적화" -ForegroundColor Green
$epic3 = New-Epic "[3단계] 고급 MSA + 최적화" "epic,P3" "9/1 - 9/28 고급 MSA 패턴 및 운영 고도화"

# W10
$w10 = New-Week "[W10] 고급 MSA 패턴 (9/1 ~ 9/7)" "infra,P3" "3단계" "Kafka, SAGA, CQRS"
Add-SubIssue $epic3 $w10
$t = New-Issue "[W10] 이벤트 기반 통신 (Kafka) 구성" "infra,P3" "3단계" "서비스 간 비동기 메시지 처리`n- Kafka 브로커 구성`n- Producer / Consumer 구현`n- Dead Letter Queue 설정"
Add-SubIssue $w10 $t
$t = New-Issue "[W10] 분산 트랜잭션 (SAGA 패턴) 구현" "business,P3" "3단계" "서비스 간 데이터 일관성 보장`n- Choreography 또는 Orchestration 방식 선택`n- 보상 트랜잭션 구현"
Add-SubIssue $w10 $t
$t = New-Issue "[W10] CQRS 설계 및 구현" "business,P3" "3단계" "읽기/쓰기 모델 분리`n- Command 모델과 Query 모델 분리`n- 읽기 최적화"
Add-SubIssue $w10 $t

# W11
$w11 = New-Week "[W11] 설정 고도화 (9/8 ~ 9/14)" "infra,P3" "3단계" "Spring Cloud Config, 실시간 설정 변경"
Add-SubIssue $epic3 $w11
$t = New-Issue "[W11] Spring Cloud Config 서버 구성" "infra,P3" "3단계" "중앙 집중식 설정 관리 서버 구축`n- Config Server 모듈 추가`n- Git 기반 설정 저장소 연동"
Add-SubIssue $w11 $t
$t = New-Issue "[W11] 실시간 설정 변경 기능" "infra,P3" "3단계" "서버 재시작 없이 설정 반영`n- @RefreshScope 적용`n- /actuator/refresh 엔드포인트"
Add-SubIssue $w11 $t

# W12
$w12 = New-Week "[W12] 배포 고도화 (9/15 ~ 9/21)" "build,P3" "3단계" "Rolling Update, Blue/Green, Canary, 롤백 자동화"
Add-SubIssue $epic3 $w12
$t = New-Issue "[W12] 핫 디플로이 (Rolling Update) 구현" "build,P3" "3단계" "서비스 중단 없이 신규 버전 순차 배포`n- Graceful Shutdown 연계`n- 헬스체크 통과 후 트래픽 전환"
Add-SubIssue $w12 $t
$t = New-Issue "[W12] Blue / Green 배포 구성" "build,P3" "3단계" "두 환경을 전환하는 무중단 배포`n- Blue(현행) / Green(신규) 환경 구성`n- Gateway 레벨 트래픽 전환"
Add-SubIssue $w12 $t
$t = New-Issue "[W12] Canary 배포 전략 적용" "build,P3" "3단계" "일부 트래픽만 신규 버전으로 전환`n- 가중치 기반 라우팅 설정`n- 단계적 트래픽 증가 (10% → 50% → 100%)"
Add-SubIssue $w12 $t
$t = New-Issue "[W12] 롤백 자동화" "build,P3" "3단계" "배포 실패 시 자동 롤백`n- 헬스체크 실패 감지`n- 이전 버전 자동 복구"
Add-SubIssue $w12 $t

# W13
$w13 = New-Week "[W13] 운영 고도화 + 마무리 (9/22 ~ 9/28)" "monitoring,P3" "3단계" "ELK, 부하테스트, Contract Testing, 성능최적화, SLO"
Add-SubIssue $epic3 $w13
$t = New-Issue "[W13] ELK Stack 로그 중앙화" "monitoring,P3" "3단계" "분산 로그 수집 및 검색 시스템 구축`n- Elasticsearch + Logstash + Kibana`n- Logback → Logstash 연동"
Add-SubIssue $w13 $t
$t = New-Issue "[W13] 부하 테스트 (k6) 수행" "test,P3" "3단계" "API 성능 및 한계 측정`n- 시나리오 기반 부하 테스트 스크립트`n- TPS / 응답시간 / 에러율 측정"
Add-SubIssue $w13 $t
$t = New-Issue "[W13] Contract Testing (Pact) 구성" "test,P3" "3단계" "서비스 간 API 계약 테스트`n- Consumer 계약 정의`n- Provider 계약 검증 자동화"
Add-SubIssue $w13 $t
$t = New-Issue "[W13] 성능 최적화 (쿼리튜닝, HikariCP)" "business,P3" "3단계" "운영 환경 성능 병목 해소`n- 쿼리 실행 계획 분석 및 인덱스 추가`n- HikariCP 커넥션 풀 사이즈 최적화`n- N+1 문제 해소"
Add-SubIssue $w13 $t
$t = New-Issue "[W13] SLO / SLA 정의" "monitoring,P3" "3단계" "서비스 수준 목표 및 협약 정의`n- 가용성 목표 (예: 99.9%)`n- 응답시간 목표`n- 알람 임계값 설정"
Add-SubIssue $w13 $t

# ================================================================
# 4. 완료
# ================================================================
Write-Host "`n[4/4] 완료!" -ForegroundColor Yellow
Write-Host "================================================================" -ForegroundColor Gray
Write-Host "  Epic → 주차 → 태스크 계층 구조로 Issues 생성 완료" -ForegroundColor White
Write-Host ""
Write-Host "  다음 단계:" -ForegroundColor Cyan
Write-Host "  1. https://github.com/yooyongbeom/bx-cf-be/issues 에서 확인" -ForegroundColor Cyan
Write-Host "  2. GitHub Projects → Add item → Issues 전체 추가" -ForegroundColor Cyan
Write-Host "  3. Roadmap 뷰에서 날짜 기반 간트차트 확인" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Gray
