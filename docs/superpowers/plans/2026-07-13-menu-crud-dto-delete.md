# Menu CRUD DTO Split and Hierarchical Delete Implementation Plan

> **Superseded delete actor contract (2026-07-13):** The executed plan below originally used `ApiRequest<MenuDeleteReqDto>`. The final implementation removes that DTO and reads the authenticated actor from the Gateway-provided `X-Auth-User` header. The design spec reflects the current contract.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split menu CRUD boundary DTOs by operation and add a transactional physical delete that removes a menu subtree and its role/action associations.

**Architecture:** Preserve the current Controller → Service → Repository port → MyBatis adapter/mapper structure. PostgreSQL resolves the subtree with one recursive CTE; the service deletes association rows and menu rows in a fixed order, then records the `MENU` reference-data version change in the same transaction.

**Tech Stack:** Java 21, Spring Boot 3.3.4, Spring MVC, MyBatis, PostgreSQL, JUnit 5, AssertJ, Mockito, Gradle

## Global Constraints

- All menu HTTP endpoints use `POST`.
- Delete endpoint is `POST /menus/{menuId}/delete`.
- Delete physically removes the selected menu and every descendant.
- Delete association order is `role_menus`, `menu_actions`, then `menus`.
- Create, update, and delete return `ApiResponse<Void>`; create does not return the generated key.
- Update actor field remains `createdBy` for compatibility.
- `MenuActionResDto` and `RoleMenuSaveReqDto` remain unchanged; role-menu list responses reuse `MenuListResDto` without changing JSON fields.
- Every behavior change follows RED → GREEN → REFACTOR.

---

## File Structure

**Create**

- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuCreateReqDto.java`: create-only input fields.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuUpdateReqDto.java`: update-only editable fields.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuListResDto.java`: list response contract.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuDetailResDto.java`: detail response contract.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuDeleteReqDto.java`: delete actor input.

**Modify**

- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/controller/MenuController.java`: operation-specific DTOs and delete mapping.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuService.java`: operation-specific service signatures and delete contract.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuServiceImpl.java`: operation-specific CRUD flow and transactional subtree deletion.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/MenuRepository.java`: typed CRUD operations and subtree-delete operations.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/mybatis/MybatisMenuRepositoryAdapter.java`: mapper delegation.
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/mybatis/mapper/MenuMapper.java`: MyBatis method signatures.
- `src/main/resources/mapper/system-svc/MenuMapper.xml`: list/detail result maps, typed update parameters, recursive hierarchy query, bulk deletes.
- `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/domain/dto/SystemDtoOpenApiMetadataTests.java`: new DTO metadata coverage.
- `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/controller/SystemControllerMappingTests.java`: request generic types and delete mapping.
- `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/service/MenuServiceTests.java`: CRUD typing, validation, deletion order, errors, and version changes.
- `README.md`: document the delete endpoint.

**Delete after all consumers migrate**

- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuReqDto.java`
- `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuResDto.java`

---

### Task 1: Add Operation-Specific Menu DTOs

**Files:**

- Create: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuCreateReqDto.java`
- Create: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuUpdateReqDto.java`
- Create: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuListResDto.java`
- Create: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuDetailResDto.java`
- Create: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuDeleteReqDto.java`
- Modify: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/domain/dto/SystemDtoOpenApiMetadataTests.java`

**Interfaces:**

- Produces: `MenuCreateReqDto`, `MenuUpdateReqDto`, `MenuListResDto`, `MenuDetailResDto`, and `MenuDeleteReqDto` with Lombok accessors, MyBatis aliases, and TypeBridge metadata.
- Consumes: existing `@ApiDto`, `@ApiField`, `ApiType`, Lombok `@Data`, and MyBatis `@Alias` conventions.

- [ ] **Step 1: Write the failing DTO metadata and boundary test**

Replace the old menu DTO entries in `DTO_TYPES` with the five expected classes, and add assertions proving identifiers are not accepted in create/update/delete bodies:

```java
import com.bwg.channel.backend.systemsvc.menu.dto.MenuCreateReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDeleteReqDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDetailResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuListResDto;
import com.bwg.channel.backend.systemsvc.menu.dto.MenuUpdateReqDto;

private static final List<Class<?>> DTO_TYPES = List.of(
        CommonCodeGroupReqDto.class,
        CommonCodeGroupResDto.class,
        CommonCodeGroupDetailResDto.class,
        CommonCodeReqDto.class,
        CommonCodeResDto.class,
        MenuCreateReqDto.class,
        MenuUpdateReqDto.class,
        MenuListResDto.class,
        MenuDetailResDto.class,
        MenuDeleteReqDto.class,
        MenuActionResDto.class,
        RoleMenuSaveReqDto.class
);

@Test
void menuCommandDtosExposeOnlyTheirOperationFields() {
    assertThat(MenuCreateReqDto.class.getDeclaredFields())
            .extracting(Field::getName)
            .doesNotContain("menuId");
    assertThat(MenuUpdateReqDto.class.getDeclaredFields())
            .extracting(Field::getName)
            .doesNotContain("menuId", "menuCd");
    assertThat(MenuDeleteReqDto.class.getDeclaredFields())
            .extracting(Field::getName)
            .containsExactly("deletedBy");
}
```

Add `import java.lang.reflect.Field;`.

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```bash
./gradlew :services:system-svc:test --tests '*SystemDtoOpenApiMetadataTests'
```

Expected: compilation fails because the five new DTO classes do not exist.

- [ ] **Step 3: Implement the five DTOs**

Each request DTO source starts with these declarations:

```java
package com.bwg.channel.backend.systemsvc.menu.dto;

import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiDto;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiField;
import com.bwg.channel.backend.common.openapi.typebridge.annotation.ApiType;
import lombok.Data;
import org.apache.ibatis.type.Alias;
```

Each response DTO adds `import java.time.OffsetDateTime;`. Create `MenuCreateReqDto` and `MenuUpdateReqDto` with endpoint-specific metadata:

```java
@Alias("MenuCreateReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "MenuCreate", endpoints = {"create"})
public class MenuCreateReqDto {
    @ApiField(description = "상위 메뉴 ID", optional = {"create"})
    private Long parentMenuId;
    @ApiField(description = "메뉴 코드", example = "DASHBOARD", required = {"create"})
    private String menuCd;
    @ApiField(description = "메뉴명", example = "대시보드", required = {"create"})
    private String menuNm;
    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"create"})
    private String menuType;
    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"create"})
    private String path;
    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"create"})
    private String component;
    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"create"})
    private String icon;
    @ApiField(description = "메뉴 깊이", example = "1", optional = {"create"})
    private Integer depth;
    @ApiField(description = "정렬 순서", example = "1", optional = {"create"})
    private Integer sortSeq;
    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create"})
    private String visibleYn;
    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"create"})
    private String useYn;
    @ApiField(description = "비고", optional = {"create"})
    private String remark;
    @ApiField(description = "요청자 ID", example = "admin", optional = {"create"})
    private String createdBy;
}
```

Create `MenuUpdateReqDto` without identifiers or `menuCd`:

```java
@Alias("MenuUpdateReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "MenuUpdate", endpoints = {"update"})
public class MenuUpdateReqDto {
    @ApiField(description = "상위 메뉴 ID", optional = {"update"})
    private Long parentMenuId;
    @ApiField(description = "메뉴명", example = "대시보드", required = {"update"})
    private String menuNm;
    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"update"})
    private String menuType;
    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"update"})
    private String path;
    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"update"})
    private String component;
    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"update"})
    private String icon;
    @ApiField(description = "메뉴 깊이", example = "1", optional = {"update"})
    private Integer depth;
    @ApiField(description = "정렬 순서", example = "1", optional = {"update"})
    private Integer sortSeq;
    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"update"})
    private String visibleYn;
    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"update"})
    private String useYn;
    @ApiField(description = "비고", optional = {"update"})
    private String remark;
    @ApiField(description = "요청자 ID", example = "admin", optional = {"update"})
    private String createdBy;
}
```

Create the list response with all current menu and audit fields:

```java
@Alias("MenuListResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "MenuList", endpoints = {"list"})
public class MenuListResDto {
    @ApiField(description = "메뉴 ID", optional = {"list"})
    private Long menuId;
    @ApiField(description = "상위 메뉴 ID", optional = {"list"})
    private Long parentMenuId;
    @ApiField(description = "메뉴 코드", example = "DASHBOARD", optional = {"list"})
    private String menuCd;
    @ApiField(description = "메뉴명", example = "대시보드", optional = {"list"})
    private String menuNm;
    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"list"})
    private String menuType;
    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"list"})
    private String path;
    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"list"})
    private String component;
    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"list"})
    private String icon;
    @ApiField(description = "메뉴 깊이", example = "1", optional = {"list"})
    private Integer depth;
    @ApiField(description = "정렬 순서", example = "1", optional = {"list"})
    private Integer sortSeq;
    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String visibleYn;
    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"list"})
    private String useYn;
    @ApiField(description = "비고", optional = {"list"})
    private String remark;
    @ApiField(description = "생성자 ID", example = "admin", optional = {"list"})
    private String createdBy;
    @ApiField(description = "수정자 ID", example = "admin", optional = {"list"})
    private String updatedBy;
    @ApiField(description = "생성 일시", example = "2026-01-01T09:00:00+09:00", optional = {"list"})
    private OffsetDateTime createdAt;
    @ApiField(description = "수정 일시", example = "2026-01-01T10:00:00+09:00", optional = {"list"})
    private OffsetDateTime updatedAt;
}
```

Create the independent detail response contract:

```java
@Alias("MenuDetailResDto")
@Data
@ApiDto(type = ApiType.RESPONSE, name = "MenuDetail", endpoints = {"detail"})
public class MenuDetailResDto {
    @ApiField(description = "메뉴 ID", optional = {"detail"})
    private Long menuId;
    @ApiField(description = "상위 메뉴 ID", optional = {"detail"})
    private Long parentMenuId;
    @ApiField(description = "메뉴 코드", example = "DASHBOARD", optional = {"detail"})
    private String menuCd;
    @ApiField(description = "메뉴명", example = "대시보드", optional = {"detail"})
    private String menuNm;
    @ApiField(description = "메뉴 유형", example = "MENU", optional = {"detail"})
    private String menuType;
    @ApiField(description = "화면 경로", example = "/dashboard", optional = {"detail"})
    private String path;
    @ApiField(description = "프론트엔드 컴포넌트 경로", example = "DashboardView", optional = {"detail"})
    private String component;
    @ApiField(description = "아이콘명", example = "LayoutDashboard", optional = {"detail"})
    private String icon;
    @ApiField(description = "메뉴 깊이", example = "1", optional = {"detail"})
    private Integer depth;
    @ApiField(description = "정렬 순서", example = "1", optional = {"detail"})
    private Integer sortSeq;
    @ApiField(description = "노출 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"detail"})
    private String visibleYn;
    @ApiField(description = "사용 여부", example = "Y", allowableValues = {"Y", "N"}, optional = {"detail"})
    private String useYn;
    @ApiField(description = "비고", optional = {"detail"})
    private String remark;
    @ApiField(description = "생성자 ID", example = "admin", optional = {"detail"})
    private String createdBy;
    @ApiField(description = "수정자 ID", example = "admin", optional = {"detail"})
    private String updatedBy;
    @ApiField(description = "생성 일시", example = "2026-01-01T09:00:00+09:00", optional = {"detail"})
    private OffsetDateTime createdAt;
    @ApiField(description = "수정 일시", example = "2026-01-01T10:00:00+09:00", optional = {"detail"})
    private OffsetDateTime updatedAt;
}
```

Create the delete input:

```java
@Alias("MenuDeleteReqDto")
@Data
@ApiDto(type = ApiType.REQUEST, name = "MenuDelete", endpoints = {"delete"})
public class MenuDeleteReqDto {
    @ApiField(description = "삭제 요청자 ID", example = "admin", required = {"delete"})
    private String deletedBy;
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the same focused test. Expected: `SystemDtoOpenApiMetadataTests` passes.

- [ ] **Step 5: Commit the DTO boundary**

```bash
git add services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/domain/dto/SystemDtoOpenApiMetadataTests.java
git commit -m "refactor(menu): split CRUD DTO contracts"
```

---

### Task 2: Migrate Existing Create, Read, and Update Flows

**Files:**

- Modify: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/controller/SystemControllerMappingTests.java`
- Modify: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/service/MenuServiceTests.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/controller/MenuController.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuService.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuServiceImpl.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/MenuRepository.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/mybatis/MybatisMenuRepositoryAdapter.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/mybatis/mapper/MenuMapper.java`
- Modify: `src/main/resources/mapper/system-svc/MenuMapper.xml`

**Interfaces:**

- Consumes: the five DTOs from Task 1.
- Produces:
  - `ApiResponse<List<MenuListResDto>> getMenus()`
  - `ApiResponse<MenuDetailResDto> getMenu(Long menuId)`
  - `ApiResponse<Void> createMenu(ApiRequest<MenuCreateReqDto> request)`
  - `ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> request)`
  - `ApiResponse<List<MenuListResDto>> getMenusByRoleId(Long roleId)`
  - `int updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> request)` at repository/mapper boundaries.

- [ ] **Step 1: Update controller and service tests first**

Change controller generic assertions:

```java
assertRequestBodyType(
        MenuController.class.getDeclaredMethod("createMenu", ApiRequest.class),
        MenuCreateReqDto.class
);
assertRequestBodyType(
        MenuController.class.getDeclaredMethod("updateMenu", Long.class, ApiRequest.class),
        MenuUpdateReqDto.class
);
```

Update `MenuServiceTests` fixtures and response generics:

```java
MenuDetailResDto menu = new MenuDetailResDto();
when(menuRepository.findMenu(1L)).thenReturn(menu);
ApiResponse<MenuDetailResDto> response = menuService.getMenu(1L);

MenuListResDto firstMenu = new MenuListResDto();
MenuListResDto secondMenu = new MenuListResDto();
when(menuRepository.findMenus()).thenReturn(List.of(firstMenu, secondMenu));
ApiResponse<List<MenuListResDto>> response = menuService.getMenus();

MenuListResDto roleMenu = new MenuListResDto();
when(menuRepository.findMenusByRoleId(1L)).thenReturn(List.of(roleMenu));
ApiResponse<List<MenuListResDto>> roleResponse = menuService.getMenusByRoleId(1L);
```

Add create/update validation and delegation tests:

```java
@Test
void createsMenuWithCreateDto() {
    MenuCreateReqDto data = new MenuCreateReqDto();
    data.setMenuCd(" DASHBOARD ");
    data.setMenuNm(" 대시보드 ");
    data.setCreatedBy("admin");
    ApiRequest<MenuCreateReqDto> request = new ApiRequest<>();
    request.setData(data);

    ApiResponse<Void> response = menuService.createMenu(request);

    assertThat(response.isSuccess()).isTrue();
    assertThat(data.getMenuCd()).isEqualTo("DASHBOARD");
    assertThat(data.getMenuNm()).isEqualTo("대시보드");
    verify(menuRepository).insertMenu(request);
    verify(menuRepository).insertReferenceDataVersionHistory(
            "MENU", "CREATE", "menus", "DASHBOARD", "메뉴 등록", "admin");
    verify(menuRepository).updateReferenceDataVersion("MENU", "메뉴 등록", "admin");
}

@Test
void updatesMenuWithPathIdAndUpdateDto() {
    MenuUpdateReqDto data = new MenuUpdateReqDto();
    data.setMenuNm(" 대시보드 ");
    data.setCreatedBy("admin");
    ApiRequest<MenuUpdateReqDto> request = new ApiRequest<>();
    request.setData(data);

    ApiResponse<Void> response = menuService.updateMenu(7L, request);

    assertThat(response.isSuccess()).isTrue();
    assertThat(data.getMenuNm()).isEqualTo("대시보드");
    verify(menuRepository).updateMenu(7L, request);
    verify(menuRepository).insertReferenceDataVersionHistory(
            "MENU", "UPDATE", "menus", "7", "메뉴 수정", "admin");
    verify(menuRepository).updateReferenceDataVersion("MENU", "메뉴 수정", "admin");
}
```

- [ ] **Step 2: Run focused tests and verify RED**

```bash
./gradlew :services:system-svc:test --tests '*SystemControllerMappingTests' --tests '*MenuServiceTests'
```

Expected: compilation/type failures because production boundaries still use `MenuReqDto` and `MenuResDto`.

- [ ] **Step 3: Migrate controller and service contracts**

Replace old DTO imports and signatures in `MenuController`, `MenuService`, and `MenuServiceImpl` with the produced interfaces above. In update, do not mutate a DTO identifier:

```java
public ApiResponse<Void> updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto) {
    MenuUpdateReqDto data = requireData(paramDto);
    Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
    data.setMenuNm(BusinessValidator.requireNonBlank(data.getMenuNm(), "menuNm"));
    menuRepository.updateMenu(requiredMenuId, paramDto);
    recordMenuVersionChange(
            "UPDATE", "menus", String.valueOf(requiredMenuId), "메뉴 수정", data.getCreatedBy());
    return ApiResponse.success(null);
}
```

Create flow stays behaviorally identical but consumes `MenuCreateReqDto`. List/detail use their dedicated response types.

- [ ] **Step 4: Migrate repository, adapter, mapper, and XML contracts**

Use exact repository and mapper signatures:

```java
List<MenuListResDto> findMenus();
MenuDetailResDto findMenu(Long menuId);
int insertMenu(ApiRequest<MenuCreateReqDto> paramDto);
int updateMenu(Long menuId, ApiRequest<MenuUpdateReqDto> paramDto);
List<MenuListResDto> findMenusByRoleId(Long roleId);
```

Mapper update signature:

```java
int updateMenu(
        @Param("menuId") Long menuId,
        @Param("request") ApiRequest<MenuUpdateReqDto> paramDto
);
```

Adapter delegates `menuMapper.updateMenu(menuId, paramDto)`.

In XML:

- Duplicate the existing `menuResult` into `menuListResult` with type `MenuListResDto` and `menuDetailResult` with type `MenuDetailResDto`.
- Point `findMenus` at `menuListResult` and `findMenu` at `menuDetailResult`.
- Point `findMenusByRoleId` at `menuListResult` while preserving its SQL and JSON fields.
- Keep insert expressions as `#{data.*}`.
- Change update expressions to `#{request.data.*}` and the predicate to `WHERE menu_id = #{menuId}`.

- [ ] **Step 5: Run focused tests and verify GREEN**

Run the Task 2 focused command. Expected: controller and service tests pass.

- [ ] **Step 6: Commit the CRU migration**

```bash
git add services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/controller/SystemControllerMappingTests.java services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/service/MenuServiceTests.java src/main/resources/mapper/system-svc/MenuMapper.xml
git commit -m "refactor(menu): use operation-specific CRUD DTOs"
```

---

### Task 3: Add Transactional Hierarchical Physical Delete

**Files:**

- Modify: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/controller/SystemControllerMappingTests.java`
- Modify: `services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/service/MenuServiceTests.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/controller/MenuController.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuService.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/service/MenuServiceImpl.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/MenuRepository.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/mybatis/MybatisMenuRepositoryAdapter.java`
- Modify: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/repository/mybatis/mapper/MenuMapper.java`
- Modify: `src/main/resources/mapper/system-svc/MenuMapper.xml`

**Interfaces:**

- Consumes: `MenuDeleteReqDto`, `BusinessValidator`, and existing version-history methods.
- Produces:
  - `ApiResponse<Void> deleteMenu(Long menuId, ApiRequest<MenuDeleteReqDto> request)`
  - `List<Long> findMenuHierarchyIds(Long menuId)`
  - `int deleteRoleMenusByMenuIds(List<Long> menuIds)`
  - `int deleteMenuActionsByMenuIds(List<Long> menuIds)`
  - `int deleteMenus(List<Long> menuIds)`

- [ ] **Step 1: Write failing controller contract tests**

```java
@Test
void menuDeleteUsesPathVariablePostMapping() throws NoSuchMethodException {
    Method method = MenuController.class.getDeclaredMethod("deleteMenu", Long.class, ApiRequest.class);
    PostMapping mapping = method.getAnnotation(PostMapping.class);

    assertThat(mapping.value()).containsExactly("/{menuId}/delete");
    assertThat(method.getParameters()[0].isAnnotationPresent(PathVariable.class)).isTrue();
    assertRequestBodyType(method, MenuDeleteReqDto.class);
}
```

- [ ] **Step 2: Write failing service deletion tests**

Add these exact imports, then test the complete order:

```java
import com.bwg.channel.backend.systemsvc.menu.dto.MenuDeleteReqDto;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
```

```java
@Test
void deletesMenuHierarchyAssociationsAndVersionInOrder() {
    MenuDeleteReqDto data = new MenuDeleteReqDto();
    data.setDeletedBy("admin");
    ApiRequest<MenuDeleteReqDto> request = new ApiRequest<>();
    request.setData(data);
    List<Long> menuIds = List.of(10L, 11L, 12L);
    when(menuRepository.findMenuHierarchyIds(10L)).thenReturn(menuIds);

    ApiResponse<Void> response = menuService.deleteMenu(10L, request);

    assertThat(response.isSuccess()).isTrue();
    InOrder inOrder = inOrder(menuRepository);
    inOrder.verify(menuRepository).findMenuHierarchyIds(10L);
    inOrder.verify(menuRepository).deleteRoleMenusByMenuIds(menuIds);
    inOrder.verify(menuRepository).deleteMenuActionsByMenuIds(menuIds);
    inOrder.verify(menuRepository).deleteMenus(menuIds);
    inOrder.verify(menuRepository).insertReferenceDataVersionHistory(
            "MENU", "DELETE", "menus", "10", "메뉴 삭제", "admin");
    inOrder.verify(menuRepository).updateReferenceDataVersion("MENU", "메뉴 삭제", "admin");
}
```

Test missing target:

```java
@Test
void rejectsMissingMenuHierarchyBeforeDelete() {
    MenuDeleteReqDto data = new MenuDeleteReqDto();
    data.setDeletedBy("admin");
    ApiRequest<MenuDeleteReqDto> request = new ApiRequest<>();
    request.setData(data);
    when(menuRepository.findMenuHierarchyIds(99L)).thenReturn(List.of());

    assertThatThrownBy(() -> menuService.deleteMenu(99L, request))
            .isInstanceOf(BwgBusinessException.class)
            .extracting("code")
            .isEqualTo(BusinessErrorCode.BUSINESS_DATA_NOT_FOUND);

    verify(menuRepository, never()).deleteRoleMenusByMenuIds(anyList());
    verify(menuRepository, never()).deleteMenuActionsByMenuIds(anyList());
    verify(menuRepository, never()).deleteMenus(anyList());
}
```

Test missing actor:

```java
@Test
void rejectsDeleteWithoutDeletedBy() {
    MenuDeleteReqDto data = new MenuDeleteReqDto();
    ApiRequest<MenuDeleteReqDto> request = new ApiRequest<>();
    request.setData(data);

    assertThatThrownBy(() -> menuService.deleteMenu(10L, request))
            .isInstanceOf(BwgBusinessException.class)
            .extracting("code")
            .isEqualTo(BusinessErrorCode.REQUIRED_VALUE_MISSING);

    verifyNoInteractions(menuRepository);
}
```

- [ ] **Step 3: Run focused tests and verify RED**

```bash
./gradlew :services:system-svc:test --tests '*SystemControllerMappingTests' --tests '*MenuServiceTests'
```

Expected: compilation fails because delete methods are absent.

- [ ] **Step 4: Add controller, service interface, and service implementation**

Controller:

```java
@Operation(summary = "메뉴 삭제")
@PostMapping("/{menuId}/delete")
public ApiResponse<Void> deleteMenu(
        @PathVariable Long menuId,
        @RequestBody ApiRequest<MenuDeleteReqDto> req
) {
    return menuService.deleteMenu(menuId, req);
}
```

Service implementation:

```java
@Override
@Transactional(transactionManager = "mybatisMainTransactionManager")
public ApiResponse<Void> deleteMenu(Long menuId, ApiRequest<MenuDeleteReqDto> paramDto) {
    Long requiredMenuId = BusinessValidator.requireNonNull(menuId, "menuId");
    MenuDeleteReqDto data = requireData(paramDto);
    String deletedBy = BusinessValidator.requireNonBlank(data.getDeletedBy(), "deletedBy");
    List<Long> menuIds = menuRepository.findMenuHierarchyIds(requiredMenuId);
    List<Long> requiredMenuIds = BusinessValidator.requireFound(
            menuIds == null || menuIds.isEmpty() ? null : menuIds,
            "menu"
    );
    menuRepository.deleteRoleMenusByMenuIds(requiredMenuIds);
    menuRepository.deleteMenuActionsByMenuIds(requiredMenuIds);
    menuRepository.deleteMenus(requiredMenuIds);
    recordMenuVersionChange(
            "DELETE", "menus", String.valueOf(requiredMenuId), "메뉴 삭제", deletedBy);
    return ApiResponse.success(null);
}
```

Use the validated `requiredMenuIds` list for every delete call; do not introduce a new exception type.

- [ ] **Step 5: Add repository, adapter, and mapper contracts**

Repository declarations:

```java
List<Long> findMenuHierarchyIds(Long menuId);
int deleteRoleMenusByMenuIds(List<Long> menuIds);
int deleteMenuActionsByMenuIds(List<Long> menuIds);
int deleteMenus(List<Long> menuIds);
```

Mapper declarations use explicit names:

```java
List<Long> findMenuHierarchyIds(@Param("menuId") Long menuId);
int deleteRoleMenusByMenuIds(@Param("menuIds") List<Long> menuIds);
int deleteMenuActionsByMenuIds(@Param("menuIds") List<Long> menuIds);
int deleteMenus(@Param("menuIds") List<Long> menuIds);
```

Adapter methods delegate one-to-one to `MenuMapper`.

- [ ] **Step 6: Add recursive hierarchy and bulk delete SQL**

Add to `MenuMapper.xml`:

```xml
<select id="findMenuHierarchyIds" resultType="long">
    <![CDATA[
    /* systemsvc.MenuMapper.findMenuHierarchyIds(메뉴 계층 ID 조회) */
    WITH RECURSIVE menu_tree AS (
        SELECT menu_id
        FROM menus
        WHERE menu_id = #{menuId}
        UNION
        SELECT child.menu_id
        FROM menus child
        INNER JOIN menu_tree parent
           ON child.parent_menu_id = parent.menu_id
    )
    SELECT menu_id
    FROM menu_tree
    ]]>
</select>

<delete id="deleteRoleMenusByMenuIds">
    DELETE FROM role_menus
    WHERE menu_id IN
    <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">
        #{menuId}
    </foreach>
</delete>

<delete id="deleteMenuActionsByMenuIds">
    DELETE FROM menu_actions
    WHERE menu_id IN
    <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">
        #{menuId}
    </foreach>
</delete>

<delete id="deleteMenus">
    DELETE FROM menus
    WHERE menu_id IN
    <foreach collection="menuIds" item="menuId" open="(" separator="," close=")">
        #{menuId}
    </foreach>
</delete>
```

Keep the existing role-based `deleteRoleMenus(Long roleId)` SQL unchanged because role-menu save still uses it.

- [ ] **Step 7: Run focused tests and verify GREEN**

Run the Task 3 focused command. Expected: controller and service tests pass, including deletion ordering and validation.

- [ ] **Step 8: Commit hierarchical delete**

```bash
git add services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/controller/SystemControllerMappingTests.java services/system-svc/src/test/java/com/bwg/channel/backend/systemsvc/service/MenuServiceTests.java src/main/resources/mapper/system-svc/MenuMapper.xml
git commit -m "feat(menu): delete menu hierarchy"
```

---

### Task 4: Remove Legacy DTOs and Verify Documentation/Regression

**Files:**

- Delete: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuReqDto.java`
- Delete: `services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto/MenuResDto.java`
- Modify: `README.md`
- Verify: all menu production/test sources and `MenuMapper.xml`

**Interfaces:**

- Consumes: all migrated DTO contracts from Tasks 1–3.
- Produces: no remaining production/test reference to `MenuReqDto` or `MenuResDto`; documented delete endpoint; green module and repository-wide tests.

- [ ] **Step 1: Prove legacy DTO references still exist before removal**

```bash
rg -n "MenuReqDto|MenuResDto" services/system-svc src/main/resources/mapper/system-svc
```

Expected: only the two legacy DTO declarations remain. If consumers remain, migrate them before deletion.

- [ ] **Step 2: Delete the two legacy DTO files**

Use `apply_patch` to remove `MenuReqDto.java` and `MenuResDto.java`.

- [ ] **Step 3: Document the endpoint**

Add this line to the System - Menu endpoint block immediately after update:

```text
POST /channel/backend/api/v1/system/menus/{menuId}/delete
```

- [ ] **Step 4: Check source and XML consistency**

```bash
rg -n "MenuReqDto|MenuResDto" services/system-svc src/main/resources/mapper/system-svc
```

Expected: no matches.

```bash
git diff --check
```

Expected: no whitespace errors.

- [ ] **Step 5: Run system-svc verification**

```bash
./gradlew :services:system-svc:test
```

Expected: `BUILD SUCCESSFUL` and all system service tests pass.

- [ ] **Step 6: Run full regression verification**

```bash
./gradlew test
```

Expected: `BUILD SUCCESSFUL` and all repository tests pass.

- [ ] **Step 7: Review the final diff and status**

```bash
git diff --stat HEAD
git status --short
```

Expected: only the intended menu CRUD/delete implementation, tests, README, and this plan are present.

- [ ] **Step 8: Commit cleanup and documentation**

```bash
git add README.md services/system-svc/src/main/java/com/bwg/channel/backend/systemsvc/menu/dto docs/superpowers/plans/2026-07-13-menu-crud-dto-delete.md
git commit -m "docs(menu): document CRUD DTO and delete API"
```

---

## Final Verification Checklist

- [ ] Create accepts only `MenuCreateReqDto` and returns `ApiResponse<Void>`.
- [ ] Update accepts only `MenuUpdateReqDto`; `menuId` remains a PathVariable and `menuCd` cannot be updated.
- [ ] List returns `List<MenuListResDto>`.
- [ ] Detail returns `MenuDetailResDto` and preserves not-found behavior.
- [ ] Delete requires `MenuDeleteReqDto.deletedBy`.
- [ ] Delete resolves and removes the full menu subtree.
- [ ] Role-menu and menu-action associations are deleted before menus.
- [ ] Delete version history and latest version update share the delete transaction.
- [ ] Existing role-menu save behavior still works.
- [ ] Old shared menu DTOs have no remaining references.
- [ ] Focused, module, and full test commands all pass.
