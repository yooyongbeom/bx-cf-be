# 프로젝트 지침

공통 규칙은 아래 파일을 따른다.

@AGENTS.md

## Superpowers 작업 경로

- Superpowers 관련 설계, 계획, 문서를 참조하거나 생성·수정할 때는 `C:\work\99_superpowers`를 기준 경로로 사용한다.
- 구현 계획은 `C:\work\99_superpowers\plans`, 설계 문서는 `C:\work\99_superpowers\specs`에서 관리한다.
- 프로젝트 내부의 `docs\superpowers` 경로는 참조하거나 새로 생성하지 않는다.

## 코드 작성 및 검증

- 코드를 작성할 때는 기존 코드 스타일을 참고하여 로직마다 의도를 설명하는 주석을 작성한다.
- 별도 요청이 없으면 작업 중에는 컴파일 여부만 확인하고 테스트는 실행하지 않는다.

## API 설계

- API는 등록, 조회, 수정, 삭제(CRUD)를 각각 제공한다.
- 등록 API는 등록 대상의 키를 URL 경로 매개변수로 요구하지 않고 마지막 endpoint를 `create`로 한다. 다만, 마스터에 종속된 데이터를 등록할 때는 마스터 키를 URL 경로 매개변수로 포함한다.
- 수정과 삭제 API는 대상 키를 URL 경로 매개변수로 포함하고 마지막 endpoint를 각각 `update`, `delete`로 한다.
- 목록 조회 API는 조회 대상 자체의 키를 URL 경로 매개변수로 사용하지 않고 마지막 endpoint를 `list`로 한다. 다만, 마스터에 종속된 상세 목록은 조회 범위를 특정하기 위해 마스터 키를 URL 경로 매개변수로 포함한다. 예: `/groups/{groupCd}/codes/list`
- 상세 조회 API는 키를 URL 경로 매개변수로 포함하고 마지막 endpoint를 `detail`로 한다.
- 마스터·디테일 관계의 데이터를 함께 처리하는 API는 앞쪽 리소스 path를 생략한다. 예를 들어 공통코드의 그룹코드와 상세코드를 함께 처리하는 등록 API는 `/groups/create`가 아니라 `/create`로 한다.
- 단건 조회 응답의 `payload`는 JSON object로 제공하고, 목록 또는 복수 건 조회 응답의 `payload`는 JSON array로 제공한다.
- 단건 응답 DTO 내부에는 업무상 필요한 배열 필드를 포함할 수 있지만, 단건 DTO 자체를 `List`로 감싸 `payload`를 배열로 만들지 않는다.
- 컨트롤러와 서비스의 `ApiResponse<T>` 제네릭 타입은 실제 `payload`의 단건·목록 구조와 일치시켜야 하며, 캐스팅으로 응답 구조를 우회하지 않는다.

## 시스템 필드 처리

- API 요청 DTO와 요청 body에는 `createdBy`, `createdAt`, `updatedBy`, `updatedAt`을 포함하지 않는다.
- `createdBy`와 `updatedBy`는 Gateway가 검증된 JWT subject로 주입한 `X-Auth-User`를 사용하며, 클라이언트가 전달한 시스템 사용자 값을 신뢰하지 않는다.
- Gateway는 JWT의 세션 ID로 Redis 세션 활성 여부를 검증하고, 외부 요청의 `X-Auth-User`를 검증된 JWT subject로 덮어쓴다.
- `createdAt`과 `updatedAt`은 요청에서 받지 않고 SQL의 `current_timestamp` 또는 `now()`로 생성한다.
- 응답 DTO에는 조회 목적으로 필요한 시스템 필드를 포함할 수 있다.
