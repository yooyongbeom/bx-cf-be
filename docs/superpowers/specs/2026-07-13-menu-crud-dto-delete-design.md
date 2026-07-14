# 메뉴 CRUD DTO 분리 및 계층 삭제 설계

## 목표

메뉴 관리의 등록, 수정, 목록, 상세 DTO를 용도별로 분리하고, 선택한 메뉴와 모든 하위 메뉴를 연관 데이터와 함께 물리 삭제하는 API를 추가한다.

## 범위

- 메뉴 등록 요청 DTO와 수정 요청 DTO를 분리한다.
- 메뉴 목록 응답 DTO와 상세 응답 DTO를 분리한다.
- 메뉴 변경 작업자는 요청 payload에 포함하지 않고 Gateway가 검증해 전달하는 `X-Auth-User` 헤더를 사용한다.
- 생성, 수정, 삭제 응답은 기존과 동일하게 `ApiResponse<Void>`를 사용한다.
- 메뉴 기능 조회 DTO는 변경하지 않는다. 역할별 메뉴 저장 요청 DTO에서는 작업자 필드를 제거한다.
- 역할별 메뉴 목록 응답은 메뉴 목록과 동일한 `MenuListResDto`를 사용한다. JSON 필드 계약은 변경하지 않는다.
- 공통코드 등 다른 도메인은 변경하지 않는다.

## API 계약

### 등록

- 경로: `POST /menus/create`
- 요청: `ApiRequest<MenuCreateReqDto>`
- 응답: `ApiResponse<Void>`
- 필수값: `menuCd`, `menuNm`
- 생성 키는 응답하지 않는다.

### 수정

- 경로: `POST /menus/{menuId}/update`
- 요청: `ApiRequest<MenuUpdateReqDto>`
- 응답: `ApiResponse<Void>`
- 필수값: PathVariable `menuId`, `menuNm`
- `menuId`와 수정할 수 없는 `menuCd`는 요청 DTO에 포함하지 않는다.

### 목록

- 경로: `POST /menus/list`
- 요청 본문: 없음
- 응답: `ApiResponse<List<MenuListResDto>>`
- 현재처럼 전체 목록을 정렬하여 반환하고, 결과 크기로 단일 페이지 메타데이터를 생성한다.

### 상세

- 경로: `POST /menus/{menuId}/detail`
- 요청 본문: 없음
- 응답: `ApiResponse<MenuDetailResDto>`
- 메뉴가 없으면 `BUSINESS_DATA_NOT_FOUND`를 발생시킨다.

### 역할별 메뉴 목록

- 경로: `POST /menus/roles/{roleId}/list`
- 응답: `ApiResponse<List<MenuListResDto>>`
- 기존 메뉴 목록 필드를 그대로 유지하며 구형 `MenuResDto` 의존성만 제거한다.

### 삭제

- 경로: `POST /menus/{menuId}/delete`
- 요청 본문: 없음
- 응답: `ApiResponse<Void>`
- 필수값: PathVariable `menuId`, 내부 인증 헤더 `X-Auth-User`
- 대상 메뉴가 없으면 `BUSINESS_DATA_NOT_FOUND`를 발생시킨다.

Gateway 외부 경로는 기존 규칙에 따라 `/channel/backend/api/v1/system/menus/**`를 사용한다. 프로젝트 규칙에 맞춰 모든 엔드포인트는 `POST`로 유지한다.

## DTO 설계

### MenuCreateReqDto

등록에 필요한 다음 필드를 가진다.

- `parentMenuId`
- `menuCd`
- `menuNm`
- `menuType`
- `path`
- `component`
- `icon`
- `depth`
- `sortSeq`
- `visibleYn`
- `useYn`
- `remark`

### MenuUpdateReqDto

수정 가능한 다음 필드를 가진다.

- `parentMenuId`
- `menuNm`
- `menuType`
- `path`
- `component`
- `icon`
- `depth`
- `sortSeq`
- `visibleYn`
- `useYn`
- `remark`
`menuId`는 PathVariable로만 전달하며 `menuCd`는 수정 대상에서 제외한다. 감사 작업자는 요청 DTO에 포함하지 않는다.

### MenuListResDto

현재 메뉴 목록 응답의 모든 메뉴 필드와 감사 필드를 유지한다.

- 메뉴 필드: `menuId`, `parentMenuId`, `menuCd`, `menuNm`, `menuType`, `path`, `component`, `icon`, `depth`, `sortSeq`, `visibleYn`, `useYn`, `remark`
- 감사 필드: `createdBy`, `updatedBy`, `createdAt`, `updatedAt`

### MenuDetailResDto

현재 메뉴 상세 응답의 모든 메뉴 필드와 감사 필드를 유지한다. 초기 필드 구성은 `MenuListResDto`와 같지만, 향후 목록과 상세 계약이 독립적으로 변경될 수 있도록 별도 타입으로 둔다.

## 계층 및 데이터 흐름

기존 계층 구조를 유지한다.

1. `MenuController`가 URL, PathVariable, 요청 DTO와 변경 API의 내부 인증 헤더를 받는다.
2. `MenuService`와 `MenuServiceImpl`이 입력 검증, 트랜잭션, 기준정보 버전 변경을 담당한다.
3. `MenuRepository`가 메뉴 데이터 접근 계약을 정의한다.
4. `MybatisMenuRepositoryAdapter`가 저장소 계약을 `MenuMapper`로 연결한다.
5. `MenuMapper.xml`이 PostgreSQL SQL과 DTO 결과 매핑을 담당한다.

## 물리 삭제 설계

삭제는 `mybatisMainTransactionManager` 트랜잭션 하나에서 처리한다.

1. `menuId`와 Gateway가 전달한 `X-Auth-User`를 검증한다.
2. PostgreSQL `WITH RECURSIVE`를 사용해 루트 메뉴와 모든 하위 메뉴 ID를 조회한다.
3. 조회 결과가 비어 있으면 `BUSINESS_DATA_NOT_FOUND`를 발생시킨다.
4. 조회된 메뉴 ID에 연결된 `role_menus`를 일괄 삭제한다.
5. 조회된 메뉴 ID에 연결된 `menu_actions`를 일괄 삭제한다.
6. 조회된 루트 및 하위 `menus` 행을 일괄 삭제한다.
7. 기준정보 버전 이력에 `changeType=DELETE`, `targetTable=menus`, `targetId=루트 menuId`, `changeSummary=메뉴 삭제`를 기록한다.
8. `MENU` 기준정보 최신 버전을 증가시킨다.

연관 테이블을 먼저 삭제하여 외래키 제약조건과 무관하게 명시적인 삭제 순서를 보장한다. 메뉴 계층은 애플리케이션에서 재귀 호출하지 않고 한 번의 재귀 CTE 조회로 확정하여 쿼리 수가 트리 깊이에 따라 증가하지 않도록 한다.

## SQL 및 매핑

- 등록 SQL은 `ApiRequest<MenuCreateReqDto>`의 `data` 필드와 Service가 전달한 인증 작업자를 사용한다.
- 수정 SQL은 별도 `menuId`, 인증 작업자 Mapper 파라미터와 `ApiRequest<MenuUpdateReqDto>`를 사용한다. 수정 DTO에 식별자나 작업자를 주입하지 않는다.
- 목록과 상세은 각각 `MenuListResDto`, `MenuDetailResDto` 전용 resultMap을 사용한다.
- 재귀 조회 결과는 `List<Long>`으로 반환한다.
- 연관 데이터 및 메뉴 삭제는 `List<Long>`을 MyBatis `foreach`로 전달하여 일괄 처리한다.
- 각 삭제 Mapper는 기존 저장소 관례에 따라 영향받은 행 수를 `int`로 반환한다. Service는 이번 범위에서 이 값을 별도로 비교하거나 새로운 오류 코드로 변환하지 않는다.

## 오류 처리

- 누락된 `data`, `menuId`, `menuCd`, `menuNm`, `X-Auth-User`는 기존 `BusinessValidator`의 `REQUIRED_VALUE_MISSING`을 사용한다.
- 상세 또는 삭제 대상 메뉴가 없으면 `BUSINESS_DATA_NOT_FOUND`를 사용한다.
- SQL 실행 중 외래키나 데이터베이스 오류가 발생하면 트랜잭션 전체를 롤백하여 일부 데이터만 삭제되는 상태를 방지한다.

## 테스트 설계

### 컨트롤러 매핑

- 모든 메뉴 API가 계속 `POST`만 사용하는지 검증한다.
- 등록 요청이 `ApiRequest<MenuCreateReqDto>`인지 검증한다.
- 수정 요청이 `ApiRequest<MenuUpdateReqDto>`인지 검증한다.
- 삭제 경로가 `/{menuId}/delete`이고 `menuId`가 PathVariable인지 검증한다.
- 등록·수정·삭제·역할 메뉴 저장이 `X-Auth-User`를 요구하며 삭제 요청에는 request body가 없는지 검증한다.

### 서비스

- 목록이 `List<MenuListResDto>`와 pagination을 반환하는지 검증한다.
- 상세가 `MenuDetailResDto`를 반환하며 미존재 시 업무 예외를 발생시키는지 검증한다.
- 등록 시 필수값 검증, 저장, `CREATE` 버전 변경이 실행되는지 검증한다.
- 수정 시 PathVariable ID와 필수값 검증, 저장, `UPDATE` 버전 변경이 실행되는지 검증한다.
- 삭제 시 루트와 하위 메뉴 ID를 확보한 후 역할 메뉴, 메뉴 기능, 메뉴 순서로 삭제하는지 검증한다.
- 삭제 후 `DELETE` 버전 이력과 최신 버전 갱신이 실행되는지 검증한다.
- 삭제 대상이 없을 때 어떤 삭제 SQL이나 버전 변경도 실행하지 않는지 검증한다.
- 등록·수정·삭제·역할별 메뉴 저장과 버전 이력에 인증 헤더의 작업자가 사용되는지 검증한다.
- 기존 메뉴 기능 조회와 역할별 메뉴 저장 테스트가 계속 통과하는지 확인한다.

### 회귀 검증

- `system-svc` 테스트 전체를 실행한다.
- 전체 Gradle 테스트를 실행해 공통 OpenAPI 메타데이터와 다른 모듈의 컴파일 회귀를 확인한다.

## 완료 조건

- 메뉴 CRUD가 승인된 용도별 DTO를 사용한다.
- 메뉴 삭제 API가 루트와 모든 하위 메뉴 및 연결된 권한·기능을 물리 삭제한다.
- 미존재 및 필수값 오류가 기존 업무 오류 규칙을 따른다.
- 삭제와 버전 변경이 동일 트랜잭션에 포함된다.
- 관련 테스트와 전체 회귀 테스트가 통과한다.
